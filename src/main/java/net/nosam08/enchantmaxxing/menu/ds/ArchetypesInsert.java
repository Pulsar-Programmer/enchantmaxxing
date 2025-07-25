package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import net.minecraft.enchantment.Enchantment;

public class ArchetypesInsert {
    HashMap<Enchantment, ArrayList<Enchantment>> inner = new HashMap<>();

    /** Inserts into the Archetype Portal. Only insert when A is compatible with B. */
    public void lac_insert(Enchantment a, Enchantment b){
        ///Sort enchantments.
        List<Enchantment> enchantments = List.of(a, b);
        enchantments.sort(Comparator.comparing(Enchantment::toString)); // or use .getTranslationKey()

        final var one = enchantments.get(0);
        final var two = enchantments.get(1);

        inner.getOrDefault(one, new ArrayList<>()).add(two);
    }
}