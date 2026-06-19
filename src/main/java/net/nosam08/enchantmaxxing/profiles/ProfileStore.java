package net.nosam08.enchantmaxxing.profiles;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

/**
 * Per-world persistence for the enchant profiles held in {@link Enchantips#ACTIVE_TASKS}.
 *
 * Mirrors JourneyMap's layout: singleplayer worlds save under {@code config/ftt/sp/<world>},
 * multiplayer servers under {@code config/ftt/mp/<server>}. The active profiles are reloaded
 * when joining a world and rewritten on every change so they survive across saves and crashes.
 */
public class ProfileStore {
    private static final Path BASE = Path.of("config/ftt");

    /** Reused across saves — building a Gson per write is needless allocation. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Single background thread for disk writes so the client thread never blocks on I/O. */
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ftt-profile-io");
        t.setDaemon(true);
        return t;
    });

    static {
        // Daemon writer would be killed mid-write on game close; drain it on shutdown.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            IO.shutdown();
            try { IO.awaitTermination(5, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        }, "ftt-profile-flush"));
    }

    /** Directory for the world we are currently connected to (null when disconnected). */
    private static Path current_dir = null;
    /** Registry lookup from the active connection, needed to (de)serialize items and enchantments. */
    private static RegistryWrapper.WrapperLookup lookup = null;

    /** Resolves the per-world directory the way JourneyMap does. */
    private static Path world_dir(MinecraftClient client) {
        if (client.isInSingleplayer() && client.getServer() != null) {
            String name = client.getServer().getSaveProperties().getLevelName();
            return BASE.resolve("sp").resolve(sanitize(name));
        }
        ServerInfo server = client.getCurrentServerEntry();
        String name = server != null ? server.address : "unknown";
        return BASE.resolve("mp").resolve(sanitize(name));
    }

    /** Strips characters that are illegal or awkward in folder names. */
    private static String sanitize(String raw) {
        String cleaned = raw == null ? "" : raw.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isEmpty() ? "unknown" : cleaned;
    }

    /** Called once a world is fully joined: caches the world context and loads its profiles. */
    public static void on_join(MinecraftClient client) {
        if (client.getNetworkHandler() == null) return;
        current_dir = world_dir(client);
        lookup = client.getNetworkHandler().getRegistryManager();
        load();
    }

    /** Called on disconnect: flushes profiles and forgets the world context. */
    public static void on_disconnect() {
        save();
        current_dir = null;
        lookup = null;
        Enchantips.ACTIVE_TASKS.clear();
    }

    private static Path file() {
        return current_dir.resolve("profiles.json");
    }

    /** Serializes the current active tasks (cheap, on the caller thread) and queues the disk
     * write on the background thread. No-op when not in a world. */
    public static void save() {
        if (current_dir == null || lookup == null) return;

        RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, lookup);
        JsonArray tasks = new JsonArray();

        for (Map.Entry<ItemStackKey, EnchantmaxProfile> entry : Enchantips.ACTIVE_TASKS.entrySet()) {
            ItemStack stack = entry.getKey().inner();
            if (stack == null || stack.isEmpty()) continue;

            JsonObject task = new JsonObject();
            task.add("item", ItemStack.CODEC.encodeStart(ops, stack).getOrThrow());

            JsonArray profile = new JsonArray();
            for (EnchantmentLevelEntry ple : entry.getValue().profile) {
                JsonObject p = new JsonObject();
                p.addProperty("id", ple.enchantment().getIdAsString());
                p.addProperty("level", ple.level());
                profile.add(p);
            }
            task.add("profile", profile);

            // Persist the solved combine order so it isn't recomputed (an expensive DP) next launch.
            var solved = net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering.peek(entry.getKey(), entry.getValue());
            if (solved != null) {
                task.addProperty("order", solved.getLeft());
                task.addProperty("cost", solved.getRight());
            }

            tasks.add(task);
        }

        JsonObject root = new JsonObject();
        root.add("tasks", tasks);

        // Snapshot path + bytes now, then hand the blocking write off-thread.
        final Path dir = current_dir;
        final byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
        IO.execute(() -> write(dir, bytes));
    }

    private static void write(Path dir, byte[] bytes) {
        try {
            Files.createDirectories(dir);
            Files.write(dir.resolve("profiles.json"), bytes);
        } catch (IOException e) {
            EnchantifyClient.LOGGER.error("Failed to save enchant profiles", e);
        }
    }

    /** Replaces the in-memory active tasks with whatever is saved for this world. */
    public static void load() {
        Enchantips.ACTIVE_TASKS.clear();
        if (current_dir == null || lookup == null) return;

        Path file = file();
        if (!Files.exists(file)) return;

        RegistryOps<JsonElement> ops = RegistryOps.of(JsonOps.INSTANCE, lookup);
        RegistryWrapper.Impl<Enchantment> enchantments = lookup.getOrThrow(RegistryKeys.ENCHANTMENT);

        try {
            String content = Files.readString(file);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            JsonArray tasks = root.getAsJsonArray("tasks");
            if (tasks == null) return;

            for (JsonElement element : tasks) {
                JsonObject task = element.getAsJsonObject();

                ItemStack stack = ItemStack.CODEC.parse(ops, task.get("item")).getOrThrow();
                EnchantmaxProfile profile = new EnchantmaxProfile();

                for (JsonElement pe : task.getAsJsonArray("profile")) {
                    JsonObject p = pe.getAsJsonObject();
                    Identifier id = Identifier.tryParse(p.get("id").getAsString());
                    if (id == null) continue;
                    int level = p.get("level").getAsInt();

                    Optional<RegistryEntry.Reference<Enchantment>> ref =
                        enchantments.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, id));
                    ref.ifPresent(reference -> profile.profile.add(new EnchantmentLevelEntry(reference, level)));
                }

                if (!profile.profile.isEmpty()) {
                    ItemStackKey key = new ItemStackKey(stack);
                    Enchantips.ACTIVE_TASKS.put(key, profile);

                    // Restore the previously-solved order so the menu doesn't recompute it.
                    if (task.has("order") && task.has("cost")) {
                        net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering.seed(
                            key, profile, task.get("order").getAsString(), task.get("cost").getAsInt());
                    }
                }
            }
        } catch (Exception e) {
            EnchantifyClient.LOGGER.error("Failed to load enchant profiles", e);
        }
    }
}
