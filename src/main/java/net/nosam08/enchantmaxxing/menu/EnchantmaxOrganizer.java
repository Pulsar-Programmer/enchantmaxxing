package net.nosam08.enchantmaxxing.menu;

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

    /** Builds the ArchetypesInsert */
    public static ArchetypesInsert build_from_start(){
        var built = new ArchetypesInsert();

        var all = all_enchantments();
        for(var ench : all){
            ench.exclusiveSet().forEach((x) -> built.oa_insert(ench, x.value()));
        }

        return built;
    }
}
