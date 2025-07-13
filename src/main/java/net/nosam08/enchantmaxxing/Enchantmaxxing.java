package net.nosam08.enchantmaxxing;

import net.fabricmc.api.ModInitializer;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

public class Enchantmaxxing implements ModInitializer {
	public static final String MOD_ID = "enchantify";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static EnchantifyConfig CONFIG;

	@Override
	public void onInitialize() {
		// AutoConfig.register(EnchantifyConfig.class, JanksonConfigSerializer::new);
		
        // CONFIG = AutoConfig.getConfigHolder(EnchantifyConfig.class).getConfig();
	}
}