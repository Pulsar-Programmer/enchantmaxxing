package net.nosam08.enchantmaxxing.config;

import java.util.function.Consumer;
import java.util.function.Supplier;

import main.walksy.lib.api.WalksyLibConfig;
import main.walksy.lib.core.config.impl.ModConfig;
import main.walksy.lib.core.config.local.Category;
import main.walksy.lib.core.config.local.Option;
import main.walksy.lib.core.config.local.OptionDescription;
import main.walksy.lib.core.config.local.options.BooleanOption;
import main.walksy.lib.core.config.local.options.ColorOption;
import main.walksy.lib.core.config.local.options.EnumOption;
import main.walksy.lib.core.config.local.options.groups.OptionGroup;
import main.walksy.lib.core.config.local.options.type.WalksyLibColor;
import main.walksy.lib.core.utils.PathUtils;
import net.nosam08.enchantmaxxing.EnchantifyClient;

/**
 * Builds the WalksyLib config for FTT. Every option's getter/setter points straight at the live
 * {@link EnchantifyClient#CONFIG} instance, so the menu reads and writes the same fields the rest of
 * the mod already uses. WalksyLib serializes these values to {@code config/enchantify.json} and reloads
 * them on startup — there is no separate persistence layer anymore.
 *
 * <p>Replaces the old Cloth Config / ModMenu screen. The actual screen is rendered by WalksyLib's own
 * ModMenu integration, which discovers this config through the {@code walksylib} entrypoint
 * ({@link EnchantifyWalksyApi}).
 */
public class EnchantifyConfigDefinition implements WalksyLibConfig {

    @Override
    public ModConfig define() {
        // Defaults to compare against / reset to — a fresh instance so its values are never mutated.
        final EnchantifyConfig def = new EnchantifyConfig();
        final EnchantifyConfig cfg = EnchantifyClient.CONFIG;

        final OptionGroup general = OptionGroup.createBuilder("General")
            .addOption(bool("Max Out Items by Default",
                () -> cfg.defaultX, def.defaultX, v -> cfg.defaultX = v,
                "Skips the `X` button press required to start maxing out an item."))
            .addOption(color("Enchantment Tooltip Hover Color",
                () -> cfg.hoverColor, def.hoverColor, v -> cfg.hoverColor = v,
                "The color of needed enchantments that appear under the item."))
            .addOption(bool("Static Enchantmax Notes",
                () -> cfg.is_static, def.is_static, v -> cfg.is_static = v,
                "Determines whether to enchantmax enchantments that are already applied."))
            .build();

        final OptionGroup menu = OptionGroup.createBuilder("Menu")
            .addOption(bool("Do Afterfuse in Menu",
                () -> cfg.do_afterfuse, def.do_afterfuse, v -> cfg.do_afterfuse = v,
                "Determines whether to perform the Afterfuse Enchantment Modification Menu enhancement."))
            .addOption(bool("Anvil Apply Sound",
                () -> cfg.anvil_apply_sound, def.anvil_apply_sound, v -> cfg.anvil_apply_sound = v,
                "Determines whether to use the Anvil sound when clicking apply to your item."))
            .addOption(bool("Fancy Menu",
                () -> cfg.do_fancy_menu, def.do_fancy_menu, v -> cfg.do_fancy_menu = v,
                "Determines whether to implement the fancy menu or the regular one."))
            .addOption(bool("Force Combinable Enchantments",
                () -> cfg.force_combinable, def.force_combinable, v -> cfg.force_combinable = v,
                "Ignores enchantment exclusivity so any enchantments can be combined (e.g. all protections)."))
            .addOption(EnumOption.createBuilder("Curse Order",
                () -> cfg.curse_order, def.curse_order, v -> cfg.curse_order = v, CurseOrderOptions.class)
                .description(desc("Determines the order of the curses in the menu."))
                .build())
            .build();

        return ModConfig.createBuilder()
            .path(PathUtils.ofConfigDir("enchantify"))
            .category(Category.createBuilder("General").group(general).build())
            .category(Category.createBuilder("Menu").group(menu).build())
            .build();
    }

    /** Builds a boolean toggle wired to a config field. */
    private static Option<Boolean> bool(String name, Supplier<Boolean> getter, boolean defaultValue,
                                        Consumer<Boolean> setter, String description) {
        return BooleanOption.createBuilder(name, getter, defaultValue, setter)
            .description(desc(description))
            .build();
    }

    /** Builds a color picker wired to a config field. */
    private static Option<WalksyLibColor> color(String name, Supplier<WalksyLibColor> getter,
                                                WalksyLibColor defaultValue, Consumer<WalksyLibColor> setter,
                                                String description) {
        return ColorOption.createBuilder(name, getter, defaultValue, setter)
            .description(desc(description))
            .build();
    }

    /** Static text description shown in the option's side panel. */
    private static OptionDescription desc(String text) {
        return OptionDescription.ofOrderedString(() -> text);
    }
}
