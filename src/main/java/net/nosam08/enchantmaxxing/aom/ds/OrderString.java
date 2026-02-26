package net.nosam08.enchantmaxxing.aom.ds;

import net.minecraft.item.ItemStack;

public class OrderString {
    // /** Object is an ItemStackKey because we only care about certain properties on it, and it makes more sense for the use case. */
    public ItemStack object;
    public String ordering;

    public OrderString(ItemStack object, String ordering) {
        this.object = object;
        this.ordering = ordering;
    }
}
