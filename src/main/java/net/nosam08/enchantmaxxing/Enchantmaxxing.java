package net.nosam08.enchantmaxxing;

import net.fabricmc.api.ModInitializer;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;
import net.nosam08.enchantmaxxing.config.EnchantifyModMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchantmaxxing implements ModInitializer {
	public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static EnchantifyConfig CONFIG = EnchantifyModMenu.load();

	@Override
	public void onInitialize() {
		
	}
}