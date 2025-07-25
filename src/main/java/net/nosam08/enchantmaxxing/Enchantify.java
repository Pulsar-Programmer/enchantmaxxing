package net.nosam08.enchantmaxxing;

import net.fabricmc.api.ModInitializer;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;
import net.nosam08.enchantmaxxing.config.EnchantifyModMenu;
import net.nosam08.enchantmaxxing.menu.EnchantmaxOrganizer;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enchantify implements ModInitializer {
	public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static EnchantifyConfig CONFIG = EnchantifyModMenu.load();
	public static ArchetypesInsert archetypes_insert;

	@Override
	public void onInitialize() {
		archetypes_insert = EnchantmaxOrganizer.build_from_start();
	}
}