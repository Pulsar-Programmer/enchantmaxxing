package net.nosam08.enchantmaxxing;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.TextureStitcher.Slot;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;

public class EnchantifyClient implements ClientModInitializer {

    public static KeyBinding openGuiKey; //does OWO Lib have something to make this better?

    @Override
    public void onInitializeClient() {
        
        // openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
        //     "key.enchantify.opengui", 
        //     InputUtil.Type.KEYSYM, 
        //     GLFW.GLFW_KEY_X, 
        //     "category.enchantify"
        // ));

        // ClientTickEvents.END_CLIENT_TICK.register(client -> {
        //     while (openGuiKey.wasPressed()) {
        //         client.setScreen(null); //not sure what to put here
        //     }
        // });
        
    }


    // public ItemStack detect_hovered_item(MinecraftClient client){

    //     if(client.currentScreen == null){
    //         return ItemStack.EMPTY;
    //     }

    //     if(client.currentScreen instanceof HandledScreen<?> handledScreen){
            
    //     }
    // }
    
}
