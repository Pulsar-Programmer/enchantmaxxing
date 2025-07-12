package net.nosam08.enchantmaxxing.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class EnchantifyModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> config_screen(parent);
    }

    private Screen config_screen(Screen parent){
        return AutoConfig.getConfigScreen(EnchantifyConfig.class, parent).get();
        // ConfigBuilder builder = ConfigBuilder.create()
        //     .setParentScreen(parent)
        //     .setTitle(Text.literal("FTT Configuration"));

        // ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        // ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));

        // general.addEntry(entryBuilder
            
        // )

        // general.addEntry(entryBuilder
        //     .startColorField(Text.literal("UI Accent Color"), MyMod.CONFIG.uiAccentColor)
        //     .setDefaultValue(new Color(0, 255, 128))
        //     .setSaveConsumer(newColor -> MyMod.CONFIG.uiAccentColor = newColor)
        //     .build()
        // );

        // builder.setSavingRunnable(() -> {
        //     AutoConfig.getConfigHolder(MyModConfig.class).save();
        // });

        // return builder.build();
    }
}
