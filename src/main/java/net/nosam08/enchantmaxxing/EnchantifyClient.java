package net.nosam08.enchantmaxxing;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.nosam08.enchantmaxxing.menu.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.menu.EnchantmaxMenu;
import net.nosam08.enchantmaxxing.mixins.HandledScreenAccessor;

public class EnchantifyClient implements ClientModInitializer {

    public static KeyBinding openGuiKey; //does OWO Lib have something to make this better?

    @Override
    public void onInitializeClient() {
        
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.enchantify.opengui",
            InputUtil.Type.KEYSYM, 
            GLFW.GLFW_KEY_X, 
            "category.enchantify.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.wasPressed()) {
                on_emenu_open(client);
            }
        });




        
    }

    /** This is called when the Enchantmaxxing Menu Key is pressed. */
    public void on_emenu_open(MinecraftClient client){
        // Tests.acceptable_or_primary();
        var item = detect_hovered_item(client);

        // Enchantify.LOGGER.info("" + item);

        var instructions = EnchantmaxBuilder.build_direct(item);
        if(instructions.isEmpty()){
            return;
        }

        // client.setScreen(EnchantmaxMenu.direct(instructions)); //not sure what to put here
    }

    /** Detects the item that is hovered. */
    public ItemStack detect_hovered_item(MinecraftClient client){
        //TODO check for their current selected hot bar item if all else fails
        if(client.currentScreen != null && client.currentScreen instanceof HandledScreen<?> handledScreen){
            var cursor = handledScreen.getScreenHandler().getCursorStack();
            
            if(cursor != null){
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

        return ItemStack.EMPTY;
    }
    
}
