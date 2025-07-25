package net.nosam08.enchantmaxxing.menu;

import java.util.stream.Collectors;

import net.minecraft.registry.Registries;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {
    
    //given an item to build with
    public static void build_from_start(){
        var enchantments = Registries..stream()
            .filter(enchantment -> enchantment.isAcceptableItem(stack))
            .collect(Collectors.toList());
        
    }

}
