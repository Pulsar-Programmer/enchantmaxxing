package net.nosam08.enchantmaxxing.aom.ds;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering;

/** Represents the data structure of an enchantment that tracks its cost throughout. */
public class SimEnchantment {
    public Integer
    pwp,
    cost;
    public String identifier;

    public SimEnchantment(Integer pwp, Integer cost, String identifier){
        this.pwp = pwp;
        this.cost = cost;
        this.identifier = identifier;
    }

    /** Creates a new SimEnchantment from the Enchantment and a level. */
    public static SimEnchantment from_enchantment(EnchantmentLevelEntry enchantment){
        var cost = enchantment.getWeight().getValue();
        return new SimEnchantment(0, cost, enchantment.enchantment.getIdAsString());
    }

    /** Merges two enchantments and computes the result as another Enchantment. */
    public static SimEnchantment merged(SimEnchantment x, SimEnchantment y){
        var new_pwp = AnvilOrdering.adv(Math.max(x.pwp, y.pwp));
        return new SimEnchantment(new_pwp, x.cost + y.cost, "(" + x.identifier + "," + y.identifier + ")");
    }

    /** Clones an Enchantment for path experimentation. */
    public SimEnchantment clone(){
        return new SimEnchantment(Integer.valueOf(pwp), Integer.valueOf(cost), new String(identifier));
    }
}
