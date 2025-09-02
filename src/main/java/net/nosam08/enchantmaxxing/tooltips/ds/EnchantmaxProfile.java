package net.nosam08.enchantmaxxing.tooltips.ds;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.emm.component_data.EnchantmentButton;

public class EnchantmaxProfile {
    public ArrayList<EnchantmentLevelEntry> profile = new ArrayList<>();

    public EnchantmaxProfile(HashMap<Integer, Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>>> selected_enchantments){
        for (Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>> bucket_groups : selected_enchantments.values()) {
            for (var hm_entry : bucket_groups.getRight().entrySet()) {
                var ench = hm_entry.getKey();
                var level = hm_entry.getValue().getLeft();
                profile.add(new EnchantmentLevelEntry(ench, level));
            }
        }
    }
}
