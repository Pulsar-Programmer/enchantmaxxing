package net.nosam08.enchantmaxxing;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;
import net.nosam08.enchantmaxxing.config.EnchantifyModMenu;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchantify implements ModInitializer {
	public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static EnchantifyConfig CONFIG = EnchantifyModMenu.load();

	@Override
	public void onInitialize() {
		
	}
}