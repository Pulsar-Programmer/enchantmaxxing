package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

public class EnchantmaxMenu {


    /** Where all the UI happens based on the direct build. */
    public static Screen direct(ItemStack item){
        ArrayList<BucketGroup> instructions = EnchantmaxBuilder.build_direct(item);
        
        return null; //TODO
    }




    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen create_menu(ItemStack item){
        MenuInstructions instructions = EnchantmaxBuilder.build(item);

        return null; //TODO
    }
}
