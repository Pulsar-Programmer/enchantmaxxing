package net.nosam08.enchantmaxxing.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.nosam08.enchantmaxxing.Enchantmaxxing;

import java.nio.file.Files;
import java.nio.file.Path;
import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;

public class EnchantifyModMenu implements ModMenuApi {

    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Path path = Path.of("config/enchantify.json5");

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
            .startBooleanToggle(Text.translatable("option.enchantify.defaultX"), Enchantmaxxing.CONFIG.defaultX)
            .setDefaultValue(def.defaultX)
            .setTooltip(Text.translatable("option.enchantify.defaultX.tooltip"))
            .setSaveConsumer(newBool -> Enchantmaxxing.CONFIG.defaultX = newBool)
            .build()
        );

        general.addEntry(entryBuilder
            .startColorField(Text.translatable("option.enchantify.hoverColor"), Enchantmaxxing.CONFIG.hoverColor)
            // .setAlphaMode(true)
            .setDefaultValue(TextColor.fromRgb(def.hoverColor))
            .setTooltip(Text.translatable("option.enchantify.hoverColor.tooltip"))
            .setSaveConsumer(newColor -> Enchantmaxxing.CONFIG.hoverColor = newColor)
            .build()
        );

        builder.setSavingRunnable(() -> {
            save(Enchantmaxxing.CONFIG);
        });

        return builder.build();
    }

    /** Saves to the config folder. */
    public static void save(EnchantifyConfig config) {

        JsonElement obj = JANKSON.toJson(config);
        String result = obj.toJson(true, true); // pretty print + JSON5

        try {

            Files.createDirectories(path.getParent());  // ensure parent folders exist
            Files.write(path, result.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Loads the config and returns it. */
    public static EnchantifyConfig load(){

        try {
            if (Files.exists(path)) {
                String content = Files.readString(path);
                JsonObject obj = JANKSON.load(content);
                return JANKSON.fromJson(obj, EnchantifyConfig.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new EnchantifyConfig();
    }
}
