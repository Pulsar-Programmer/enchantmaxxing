package net.nosam08.enchantmaxxing.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.Filesystem;

public class EnchantifyModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> config_screen(parent);
    }

    /** Builds and creates the configuration screen. */
    private static Screen config_screen(Screen parent){

        final EnchantifyConfig def = new EnchantifyConfig();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("title.enchantify.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.enchantify.general"));

        general.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.defaultX"), EnchantifyClient.CONFIG.defaultX)
            .setDefaultValue(def.defaultX)
            .setTooltip(Text.translatable("option.enchantify.defaultX.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.defaultX = newBool)
            .build()
        );

        general.addEntry(entryBuilder
            .startColorField(Text.translatable("option.enchantify.hoverColor"), EnchantifyClient.CONFIG.hoverColor)
            // .setAlphaMode(true)
            .setDefaultValue(TextColor.fromRgb(def.hoverColor))
            .setTooltip(Text.translatable("option.enchantify.hoverColor.tooltip"))
            .setSaveConsumer(newColor -> EnchantifyClient.CONFIG.hoverColor = newColor)
            .build()
        );

        general.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.is_static"), EnchantifyClient.CONFIG.is_static)
            .setDefaultValue(def.is_static)
            .setTooltip(Text.translatable("option.enchantify.is_static.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.is_static = newBool)
            .build()
        );


        ConfigCategory menu = builder.getOrCreateCategory(Text.translatable("category.enchantify.menu"));

        menu.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.do_autofuse"), EnchantifyClient.CONFIG.do_afterfuse)
            .setDefaultValue(def.do_afterfuse)
            .setTooltip(Text.translatable("option.enchantify.do_autofuse.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.do_afterfuse = newBool)
            .build()
        );

        menu.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.anvil_apply_sound"), EnchantifyClient.CONFIG.anvil_apply_sound)
            .setDefaultValue(def.anvil_apply_sound)
            .setTooltip(Text.translatable("option.enchantify.anvil_apply_sound.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.anvil_apply_sound = newBool)
            .build()
        );

        menu.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.do_fancy_menu"), EnchantifyClient.CONFIG.do_fancy_menu)
            .setDefaultValue(def.do_fancy_menu)
            .setTooltip(Text.translatable("option.enchantify.do_fancy_menu.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.do_fancy_menu = newBool)
            .build()
        );

        menu.addEntry(entryBuilder
            .startBooleanToggle(Text.translatable("option.enchantify.force_combinable"), EnchantifyClient.CONFIG.force_combinable)
            .setDefaultValue(def.force_combinable)
            .setTooltip(Text.translatable("option.enchantify.force_combinable.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.force_combinable = newBool)
            .build()
        );

        menu.addEntry(entryBuilder
            .startEnumSelector(Text.translatable("option.enchantify.curse_order"), CurseOrderOptions.class, EnchantifyClient.CONFIG.curse_order)
            .setDefaultValue(def.curse_order)
            .setEnumNameProvider(option -> Text.translatable(option.toString()).withColor(((CurseOrderOptions)option).getColor()))
            .setTooltip(Text.translatable("option.enchantify.curse_order.tooltip"))
            .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.curse_order = newBool)
            .build()
        );

        ConfigCategory anvil = builder.getOrCreateCategory(Text.translatable("category.enchantify.anvil"));

        // anvil.addEntry(entryBuilder
        //     .startBooleanToggle(Text.translatable("option.enchantify.do_anvil_notes"), EnchantifyClient.CONFIG.do_anvil_notes)
        //     .setDefaultValue(def.do_anvil_notes)
        //     .setTooltip(Text.translatable("option.enchantify.do_anvil_notes.tooltip"))
        //     .setSaveConsumer(newBool -> EnchantifyClient.CONFIG.do_anvil_notes = newBool)
        //     .build()
        // );

        // anvil.addEntry(entryBuilder
        //     .startColorField(Text.translatable("option.enchantify.next_fuse_color"), EnchantifyClient.CONFIG.next_fuse_color)
        //     // .setAlphaMode(true)
        //     .setDefaultValue(TextColor.fromRgb(def.next_fuse_color))
        //     .setTooltip(Text.translatable("option.enchantify.next_fuse_color.tooltip"))
        //     .setSaveConsumer(newColor -> EnchantifyClient.CONFIG.next_fuse_color = newColor)
        //     .build()
        // );

        builder.setSavingRunnable(() -> {
            Filesystem.save_config(EnchantifyClient.CONFIG);
        });

        return builder.build();
    }
}
