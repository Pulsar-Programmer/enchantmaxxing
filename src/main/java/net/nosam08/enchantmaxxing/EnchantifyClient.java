package net.nosam08.enchantmaxxing;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.Identifier;
import net.nosam08.enchantmaxxing.aom.AnvilMenu;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.emm.EnchantmaxMenu;
import net.nosam08.enchantmaxxing.emm.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.mixins.HandledScreenAccessor;
import net.nosam08.enchantmaxxing.profiles.ProfileStore;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

public class EnchantifyClient implements ClientModInitializer {

    public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Whether either shift key is currently held. Replaces vanilla's Screen.hasShiftDown(),
     * which was removed in 1.21.9 (shift state now lives on per-event KeyInput records).
     */
    public static boolean hasShiftDown() {
        var window = Minecraft.getInstance().getWindow();
        return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT)
            || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static EnchantifyConfig CONFIG = Filesystem.load_config();

    /**
     * Controls-screen category for this mod's keybinds. As of 1.21.9 KeyMapping takes a
     * KeyMapping.Category (identified by an Identifier) instead of a translation-key String.
     * Its label resolves to "key.category.enchantify.main" — see the lang file.
     */
    public static final KeyMapping.Category KEY_CATEGORY =
        new KeyMapping.Category(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

    public static KeyMapping MAXXING = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        "key.enchantify.opengui",
        InputConstants.Type.KEYSYM, 
        GLFW.GLFW_KEY_X, 
        KEY_CATEGORY
    )); //does OWO Lib have something to make this better?

    public static KeyMapping ORDERING = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        "key.enchantify.ordering",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Y,
        KEY_CATEGORY
    ));

    public static KeyMapping GRAPH = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        "key.enchantify.graph",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN, // unbound by default — assign it in Controls
        KEY_CATEGORY
    ));

    public static ArchetypesInsert GLOBAL_ARCHETYPES;

    @Override
    public void onInitializeClient() {
        Tests.testing();

        // if (FabricLoader.getInstance().isModLoaded("modmenu")) {
        //     CONFIG = EnchantifyModMenu.load();
        // }

        ScreenEvents.BEFORE_INIT.register((client, _screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.afterKeyPress(_screen).register((screen, keyInput) -> {
                if (screen instanceof net.minecraft.client.gui.screens.ChatScreen || screen instanceof net.minecraft.client.gui.screens.DeathScreen) {
                    return;
                }
                if (MAXXING.matches(keyInput)) {
                    on_emenu_open(client);
                }
                if (GRAPH.matches(keyInput)) {
                    on_graph_open(client);
                }
            });
        });

        // LOGGER.debug("Starting request: %s".formatted(request));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (MAXXING.consumeClick()) {
                on_emenu_open(client);
            }
            while (ORDERING.consumeClick()){
                on_amenu_open(client);
            }
            while (GRAPH.consumeClick()){
                on_graph_open(client);
            }
        });

        ItemTooltipCallback.EVENT.register((stack, _context, _type, lines) -> {
            var profile = Enchantips.ACTIVE_TASKS.get(new ItemStackKey(stack));
            if(profile != null){
                Enchantips.generate_tooltips(lines, profile, stack);
            }
        });

        ClientPlayConnectionEvents.INIT.register((handler, _client) -> {
            var enchantments = handler.registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false);
            var entries = stream.map((Enchantment x) ->enchantments.wrapAsHolder(x));
            GLOBAL_ARCHETYPES = EnchantmaxBuilder.global_archetypes(entries);
        });

        // Load this world's saved profiles once joined; flush them on disconnect.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ProfileStore.on_join(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ProfileStore.on_disconnect());
    }

    /** This is called when the Anvil Menu Key is pressed. */
    public static void on_amenu_open(Minecraft client){
        client.setScreen(AnvilMenu.start());
    }

    /** Opens the order graph for the hovered item, if it has an active task. */
    public static void on_graph_open(Minecraft client){
        var item = detect_hovered_item(client);
        if(item.isEmpty()){
            return;
        }
        var key = new ItemStackKey(item);
        var profile = Enchantips.ACTIVE_TASKS.get(key);
        if(profile == null){
            return;
        }
        var order = net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering.request(key, profile);
        if(order == null){
            // Still computing on the background thread — tell the player and bail; the result is
            // cached, so pressing the key again in a moment will open the graph instantly.
            client.gui.setOverlayMessage(net.minecraft.network.chat.Component.literal("Calculating order…"), false);
            return;
        }
        client.setScreen(new net.nosam08.enchantmaxxing.aom.graph.TaskGraphMenu(order.object, order));
    }

    /** This is called when the Enchantmaxxing Menu Key is pressed. */
    public static void on_emenu_open(Minecraft client){
        var item = detect_hovered_item(client);

        if(CONFIG.defaultX){
            var default_profile = net.nosam08.enchantmaxxing.profiles.DefaultProfiles.profile_for(item);
            if(default_profile != null){
                if(client.player != null){
                    client.player.playSound(CONFIG.anvil_apply_sound ? SoundEvents.ANVIL_USE : SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
                }
                Enchantips.start_tooltips(item, default_profile);
                return;
            }
        }

        if(!CONFIG.do_afterfuse){
            var instructions = EnchantmaxBuilder.build_direct(item);
            if(instructions.isEmpty()){
                return;
            }

            // for (var bucketGroup : instructions) {
            //     Enchantify.LOGGER.info(bucketGroup.display());
            // }

            client.setScreen(EnchantmaxMenu.direct(item, instructions));
        } else {
            var instructions = EnchantmaxBuilder.build_afterfuse(item);
            if(instructions.rows.isEmpty()){
                return;
            }
            
            //build screen based on config option for autofuse
        }

        
    }

    /** Detects the item that is hovered. */
    public static ItemStack detect_hovered_item(Minecraft client){
        if(client.screen != null && client.screen instanceof AbstractContainerScreen<?> handledScreen){
            var cursor = handledScreen.getMenu().getCarried();
            
            if(!cursor.isEmpty()){
                return cursor;
            }

            var mixin = ((HandledScreenAccessor)handledScreen);
            var item = mixin.getFocusedSlot();
            
            if(item != null){
                var stack = item.getItem();
                if(stack != null){
                    return stack;
                }
            }
        }

        if(client.player == null){
            return ItemStack.EMPTY;
        }

        return client.player.getMainHandItem();
    }





    
    
}
