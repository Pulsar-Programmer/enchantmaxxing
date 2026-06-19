package net.nosam08.enchantmaxxing.config;

import main.walksy.lib.api.WalksyLibApi;
import main.walksy.lib.core.config.impl.ModConfig;

/**
 * WalksyLib entrypoint (registered under {@code walksylib} in fabric.mod.json). WalksyLib calls
 * {@link #getConfig()} during startup to discover FTT's config, load its saved values, and build the
 * ModMenu config screen. Returning the cached {@link ModConfig} keeps a single shared instance.
 */
public class EnchantifyWalksyApi implements WalksyLibApi {

    @Override
    public ModConfig getConfig() {
        return new EnchantifyConfigDefinition().getOrCreateConfig();
    }
}
