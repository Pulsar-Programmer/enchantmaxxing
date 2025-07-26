package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.enchantment.Enchantment;

public class ArchetypesInsert {
    public HashMap<Enchantment, ArrayList<Enchantment>> inner = new HashMap<>();

    /** Inserts into the Archetype Portal. Only insert when A is compatible with B. */
    // private void lac_insert(Enchantment a, Enchantment b){
    //     ///Sort enchantments.
    //     List<Enchantment> enchantments = List.of(a, b);
    //     enchantments.sort(Comparator.comparing(Enchantment::toString)); // or use .getTranslationKey()

    //     final var one = enchantments.get(0);
    //     final var two = enchantments.get(1);

    //     inner.getOrDefault(one, new ArrayList<>()).add(two);
    // }

    /** Inserts into the Archetype Portal. Only insert when A is incompatible with B. */
    public void oa_insert(Enchantment a, Enchantment b){
        inner.getOrDefault(a, new ArrayList<>()).add(b);
    }

}