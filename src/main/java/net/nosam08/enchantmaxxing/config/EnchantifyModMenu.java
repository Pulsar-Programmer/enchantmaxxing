package net.nosam08.enchantmaxxing.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.math.Color;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.nosam08.enchantmaxxing.Enchantmaxxing;

public class EnchantifyModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> config_screen(parent);
    }

    private Screen config_screen(Screen parent){
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("title.enchantify.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.enchantify.general"));

        // general.addEntry(entryBuilder
        //     .startBooleanToggle(Text.translatable("option.enchantify.defaultX"), false)
        // )

        general.addEntry(entryBuilder
            .startColorField(Text.translatable("option.enchantify.hoverColor"), Enchantmaxxing.CONFIG.hoverColor)
            .setDefaultValue(TextColor.fromRgb())
            .setSaveConsumer(newColor -> Enchantmaxxing.CONFIG.hoverColor = newColor)
            .build()
        );

        builder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(MyModConfig.class).save();
        });

        return builder.build();
    }
}
