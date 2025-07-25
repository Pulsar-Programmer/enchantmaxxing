package net.nosam08.enchantmaxxing.menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

public class EnchantmaxMenu {
    
    /** Where all the UI happens based on the{@code MenuInstructions}. */
    public static Screen create_menu(ItemStack item){
        MenuInstructions instructions = EnchantmaxBuilder.build_given_item(item);

        return null; //TODO
    }
}
