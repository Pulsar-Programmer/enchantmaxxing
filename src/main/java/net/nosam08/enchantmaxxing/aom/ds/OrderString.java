package net.nosam08.enchantmaxxing.aom.ds;

import net.minecraft.world.item.ItemStack;

public class OrderString {
    // /** Object is an ItemStackKey because we only care about certain properties on it, and it makes more sense for the use case. */
    public ItemStack object;
    public String ordering;
    public Integer cost;

    public OrderString(ItemStack object, String ordering, Integer cost) {
        this.object = object;
        this.ordering = ordering;
        this.cost = cost;
    }
}
