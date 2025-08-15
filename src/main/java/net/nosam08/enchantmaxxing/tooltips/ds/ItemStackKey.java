package net.nosam08.enchantmaxxing.tooltips.ds;

import java.util.Objects;

import net.minecraft.item.ItemStack;

public class ItemStackKey {
    private final ItemStack stack;
    
    public ItemStackKey(ItemStack stack) {
        this.stack = stack.copy(); // Make a copy to avoid mutations
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemStackKey other){
            return ItemStack.areItemsAndComponentsEqual(this.stack, other.stack);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        // Hash based on item type and components, not the exact stack
        return Objects.hash(stack.getItem(), stack.getComponents());
    }
}

