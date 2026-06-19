package net.nosam08.enchantmaxxing.tooltips.ds;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

public class ItemStackKey {
    private final ItemStack stack;
    
    public ItemStackKey(ItemStack stack) {
        this.stack = Enchantips.strip_extras(stack); // Make a copy to avoid mutations
    }

    /** Allows the reading of an ItemStackKey. */
    public void read(){
        var data = this.stack.getEnchantments();
        for (var ench_level : data.keySet()) {
            System.out.println(ench_level + ":" + data.getLevel(ench_level));
        }
    }

    public ItemStack inner(){
        return stack;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStackKey other){
            return ItemStack.isSameItemSameComponents(this.stack, other.stack);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        // Hash based on item type and components, not the exact stack
        return Objects.hash(stack.getItem(), stack.getComponents());
    }
}

