package net.nosam08.enchantmaxxing.menu;

import java.util.stream.Stream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;

/** Finds all the possible in-game enchantments and their compatibilites. */
public class EnchantmaxOrganizer {

    /** Returns all enchantments. */
    public static Iterable<Enchantment> all_enchantments(){
        return MinecraftClient.getInstance()
            .getNetworkHandler()
            .getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    /** Builds the ArchetypesInsert from a stream. */
    public static ArchetypesInsert build_from_start(Stream<Enchantment> all){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            ench.exclusiveSet().forEach(x -> built.oa_insert(ench, x.value()));
        });

        return built;
    }
}
