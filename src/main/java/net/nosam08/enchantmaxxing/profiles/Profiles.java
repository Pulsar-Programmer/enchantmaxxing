package net.nosam08.enchantmaxxing.profiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.nosam08.enchantmaxxing.EnchantifyClient;

/**
 * Named, world-independent enchant-selection presets the player can reuse across items.
 *
 * Unlike {@link ProfileStore} (which persists the per-item tasks of a single world), a profile here
 * is just a remembered set of enchantment goals — a list of (enchantment id, level) pairs — that the
 * player can re-apply to whatever item they open the Enchantmax menu on. One file per profile lives
 * under {@code config/ftt/profiles/<name>.json} so they are shared across every world.
 */
public class Profiles {
    private static final Path DIR = Path.of("config/ftt/profiles");

    /** Reused across saves — building a Gson per write is needless allocation. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** One saved enchantment goal: the enchantment's id string and the level the player wants. */
    public record Entry(String id, int level) {}

    /** Strips characters that are illegal or awkward in file names, mirroring {@link ProfileStore}. */
    public static String sanitize(String raw) {
        String cleaned = raw == null ? "" : raw.trim().replaceAll("[^a-zA-Z0-9._ -]", "_");
        return cleaned.trim();
    }

    /** Lists saved profile names (file stem, no extension), alphabetically. */
    public static List<String> list() {
        List<String> names = new ArrayList<>();
        if (!Files.exists(DIR)) return names;
        try (Stream<Path> files = Files.list(DIR)) {
            files.filter(p -> p.getFileName().toString().endsWith(".json"))
                .forEach(p -> {
                    String file = p.getFileName().toString();
                    names.add(file.substring(0, file.length() - ".json".length()));
                });
        } catch (IOException e) {
            EnchantifyClient.LOGGER.error("Failed to list enchant profiles", e);
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    /** Reads the saved goals for a profile, or an empty list if it is missing or unreadable. */
    public static List<Entry> load(String name) {
        List<Entry> entries = new ArrayList<>();
        Path file = file(name);
        if (!Files.exists(file)) return entries;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            JsonArray enchants = root.getAsJsonArray("enchants");
            if (enchants == null) return entries;
            for (JsonElement element : enchants) {
                JsonObject e = element.getAsJsonObject();
                entries.add(new Entry(e.get("id").getAsString(), e.get("level").getAsInt()));
            }
        } catch (Exception e) {
            EnchantifyClient.LOGGER.error("Failed to load enchant profile '{}'", name, e);
        }
        return entries;
    }

    /** Writes a profile's goals to disk, creating the profiles directory if needed. */
    public static void save(String name, List<Entry> entries) {
        JsonArray enchants = new JsonArray();
        for (Entry entry : entries) {
            JsonObject e = new JsonObject();
            e.addProperty("id", entry.id());
            e.addProperty("level", entry.level());
            enchants.add(e);
        }
        JsonObject root = new JsonObject();
        root.add("enchants", enchants);

        try {
            Files.createDirectories(DIR);
            Files.write(file(name), GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            EnchantifyClient.LOGGER.error("Failed to save enchant profile '{}'", name, e);
        }
    }

    /** Removes a profile's file. No-op if it does not exist. */
    public static void delete(String name) {
        try {
            Files.deleteIfExists(file(name));
        } catch (IOException e) {
            EnchantifyClient.LOGGER.error("Failed to delete enchant profile '{}'", name, e);
        }
    }

    private static Path file(String name) {
        return DIR.resolve(sanitize(name) + ".json");
    }
}
