package net.nosam08.enchantmaxxing;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
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

    public static EnchantifyConfig CONFIG = Filesystem.load_config();

    public static KeyBinding MAXXING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.enchantify.opengui",
        InputUtil.Type.KEYSYM, 
        GLFW.GLFW_KEY_X, 
        "title.enchantify.config"
    )); //does OWO Lib have something to make this better?

    public static KeyBinding ORDERING = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.enchantify.ordering",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_Y,
        "title.enchantify.config"
    ));

    public static KeyBinding GRAPH = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        "key.enchantify.graph",
        InputUtil.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN, // unbound by default — assign it in Controls
        "title.enchantify.config"
    ));

    public static ArchetypesInsert GLOBAL_ARCHETYPES;

    @Override
    public void onInitializeClient() {
        Tests.testing();

        // if (FabricLoader.getInstance().isModLoaded("modmenu")) {
        //     CONFIG = EnchantifyModMenu.load();
        // }

        ScreenEvents.BEFORE_INIT.register((client, _screen, scaledWidth, scaledHeight) -> {
			ScreenKeyboardEvents.afterKeyPress(_screen).register((screen, key, scancode, modifiers) -> {
                if (screen instanceof net.minecraft.client.gui.screen.ChatScreen || screen instanceof net.minecraft.client.gui.screen.DeathScreen) {
                    return;
                }
                if (MAXXING.matchesKey(key, scancode)) {
                    on_emenu_open(client);
                }
                if (GRAPH.matchesKey(key, scancode)) {
                    on_graph_open(client);
                }
            });
        });

        // LOGGER.debug("Starting request: %s".formatted(request));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (MAXXING.wasPressed()) {
                on_emenu_open(client);
            }
            while (ORDERING.wasPressed()){
                on_amenu_open(client);
            }
            while (GRAPH.wasPressed()){
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
            var enchantments = handler.getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false);
            var entries = stream.map((Enchantment x) ->enchantments.getEntry(x));
            GLOBAL_ARCHETYPES = EnchantmaxBuilder.global_archetypes(entries);
        });

        // Load this world's saved profiles once joined; flush them on disconnect.
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> ProfileStore.on_join(client));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ProfileStore.on_disconnect());
    }

    /** This is called when the Anvil Menu Key is pressed. */
    public static void on_amenu_open(MinecraftClient client){
        client.setScreen(AnvilMenu.start());
    }

    /** Opens the order graph for the hovered item, if it has an active task. */
    public static void on_graph_open(MinecraftClient client){
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
            if(client.player != null){
                client.player.sendMessage(net.minecraft.text.Text.literal("Calculating order…"), true);
            }
            return;
        }
        client.setScreen(new net.nosam08.enchantmaxxing.aom.graph.TaskGraphMenu(order.object, order));
    }

    /** This is called when the Enchantmaxxing Menu Key is pressed. */
    public static void on_emenu_open(MinecraftClient client){
        var item = detect_hovered_item(client);

        // Enchantify.LOGGER.info(item.toString());

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
    public static ItemStack detect_hovered_item(MinecraftClient client){
        if(client.currentScreen != null && client.currentScreen instanceof HandledScreen<?> handledScreen){
            var cursor = handledScreen.getScreenHandler().getCursorStack();
            
            if(!cursor.isEmpty()){
                return cursor;
            }

            var mixin = ((HandledScreenAccessor)handledScreen);
            var item = mixin.getFocusedSlot();
            
            if(item != null){
                var stack = item.getStack();
                if(stack != null){
                    return stack;
                }
            }
        }

        if(client.player == null){
            return ItemStack.EMPTY;
        }

        return client.player.getMainHandStack();
    }





    
    
}
