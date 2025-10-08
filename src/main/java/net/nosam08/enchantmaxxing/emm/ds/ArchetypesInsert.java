package net.nosam08.enchantmaxxing.emm.ds;

import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.enchantment.Enchantment;

public class ArchetypesInsert {
    public HashMap<Enchantment, HashSet<Enchantment>> inner = new HashMap<>();

    /** Inserts into the Archetype Portal. Only insert when A is compatible with B. */
    // private void lac_insert(Enchantment a, Enchantment b){
    //     ///Sort enchantments.
    //     List<Enchantment> enchantments = List.of(a, b);
    //     enchantments.sort(Comparator.comparing(Enchantment::toString)); // or use .getTranslationKey()

    //     final var one = enchantments.get(0);
    //     final var two = enchantments.get(1);

    //     inner.getOrDefault(one, new ArrayList<>()).add(two);
    // }

    public HashSet<Enchantment> exclusive_set(Enchantment of){
        return inner.get(of);
    }

    /** Inserts into the Archetype Portal. Only insert when A is incompatible with B. */
    public void oa_insert(Enchantment a, Enchantment b){
        var val = inner.get(a);
        if(val == null){
            inner.put(a, new HashSet<>());
            oa_insert(a, b);
            return;
        }
        val.add(b);
        inner.put(a, val);
    }

    public String display(){
        var string = new StringBuilder();
        for (var inner_x : inner.entrySet()) {
            string.append(inner_x.getKey());
            string.append(" | ");
            for (var inner_x_v_x : inner_x.getValue()) {
                string.append(inner_x_v_x.toString());
                string.append(", ");
            }

            string.append("\n");
        }
        return string.toString();
    }

}