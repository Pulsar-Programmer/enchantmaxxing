package net.nosam08.enchantmaxxing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import net.nosam08.enchantmaxxing.config.EnchantifyConfig;

public class Filesystem {
    private static final Jankson JANKSON = Jankson.builder().build();
    private static final Path path = Path.of("config/enchantify.json5");

    /** Saves to the config folder. */
    public static void save_config(EnchantifyConfig config) {

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
    public static EnchantifyConfig load_config(){

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
