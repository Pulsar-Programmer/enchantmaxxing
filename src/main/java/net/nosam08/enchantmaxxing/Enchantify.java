package net.nosam08.enchantmaxxing;

import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.Enchantment;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;
import net.nosam08.enchantmaxxing.config.EnchantifyModMenu;
import net.nosam08.enchantmaxxing.menu.EnchantmaxOrganizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchantify implements ModInitializer {
	public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static EnchantifyConfig CONFIG = EnchantifyModMenu.load();
	public static Iterable<Enchantment> all_enchantments;

	@Override
	public void onInitialize() {
		all_enchantments = EnchantmaxOrganizer.all_enchantments();
	}
}