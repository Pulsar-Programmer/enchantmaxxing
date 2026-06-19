package net.nosam08.enchantmaxxing.tooltips.ds;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.core.Holder;
import net.minecraft.util.Tuple;
import net.nosam08.enchantmaxxing.emm.component_data.EnchantmentButton;

public class EnchantmaxProfile {
    public ArrayList<EnchantmentInstance> profile = new ArrayList<>();

    /** Empty profile, used when loading from disk. */
    public EnchantmaxProfile(){}

    public EnchantmaxProfile(HashMap<Integer, Tuple<Integer, HashMap<Holder<Enchantment>, Tuple<Integer, EnchantmentButton>>>> selected_enchantments){
        for (Tuple<Integer, HashMap<Holder<Enchantment>, Tuple<Integer, EnchantmentButton>>> bucket_groups : selected_enchantments.values()) {
            for (var hm_entry : bucket_groups.getB().entrySet()) {
                var ench = hm_entry.getKey();
                var level = hm_entry.getValue().getA();
                profile.add(new EnchantmentInstance(ench, level));
            }
        }
    }
}
