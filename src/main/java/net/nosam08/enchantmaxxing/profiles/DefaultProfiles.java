package net.nosam08.enchantmaxxing.profiles;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;

/**
 * The bundled, read-only "Default" profile shown as a white option in the menu — the curated
 * best-in-slot enchant goals for each vanilla item.
 *
 * The data lives in {@code assets/enchantmaxxing/defaults.json} (one entry per item id) so it ships
 * with the mod and can't be edited or deleted like user profiles. Items without an entry (e.g. most
 * modded items) simply have no default, which the menu and {@code defaultX} both fall back on.
 */
public class DefaultProfiles {
    private static final String RESOURCE = "/assets/enchantmaxxing/defaults.json";

    /** Item id -> default goals. Parsed once from the bundled resource and cached. */
    private static Map<String, List<Profiles.Entry>> cache = null;

    private static synchronized Map<String, List<Profiles.Entry>> all() {
        if (cache != null) return cache;
        Map<String, List<Profiles.Entry>> parsed = new HashMap<>();
        try (InputStream in = DefaultProfiles.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
                for (Map.Entry<String, com.google.gson.JsonElement> item : root.entrySet()) {
                    JsonArray enchants = item.getValue().getAsJsonObject().getAsJsonArray("enchants");
                    List<Profiles.Entry> list = new ArrayList<>();
                    for (var element : enchants) {
                        JsonObject e = element.getAsJsonObject();
                        list.add(new Profiles.Entry(e.get("id").getAsString(), e.get("level").getAsInt()));
                    }
                    parsed.put(item.getKey(), list);
                }
            }
        } catch (Exception e) {
            EnchantifyClient.LOGGER.error("Failed to load bundled default profiles", e);
        }
        cache = parsed;
        return cache;
    }

    /** The curated default goals for this item, or {@code null} when none is bundled. */
    public static List<Profiles.Entry> for_item(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<Profiles.Entry> list = all().get(id);
        return (list == null || list.isEmpty()) ? null : list;
    }

    /** Whether a bundled default exists for the given item. */
    public static boolean has(ItemStack stack) {
        return for_item(stack) != null;
    }

    /**
     * Builds an applicable {@link EnchantmaxProfile} from the item's default, resolving ids against
     * the live enchantment registry. Returns {@code null} when there is no default or nothing resolves
     * (so callers can fall back to the regular menu).
     */
    public static EnchantmaxProfile profile_for(ItemStack stack) {
        List<Profiles.Entry> entries = for_item(stack);
        if (entries == null) return null;

        var registry = EnchantmaxBuilder.all_enchantments();
        // The item's current enchant levels — skip any goal it already meets so the auto-apply
        // doesn't queue an enchantment (e.g. Mending) the item already has at that level or higher.
        var existing = EnchantmaxBuilder.levels_map(stack);

        EnchantmaxProfile profile = new EnchantmaxProfile();
        for (Profiles.Entry entry : entries) {
            Identifier id = Identifier.tryParse(entry.id());
            if (id == null) continue;
            if (existing.getOrDefault(id, 0) >= entry.level()) continue;
            Optional<Holder.Reference<Enchantment>> ref =
                registry.get(id);
            ref.ifPresent(r -> profile.profile.add(new EnchantmentInstance(r, entry.level())));
        }
        return profile.profile.isEmpty() ? null : profile;
    }
}
