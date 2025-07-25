package net.nosam08.enchantmaxxing.menu;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {
    
    /** Builds the menu appearance given the item to Enchantmax. */
    public static MenuInstructions build_given_item(ItemStack item){
        var enchantments = EnchantmaxOrganizer.all_enchantments();
        Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter((Enchantment x) -> x.isSupportedItem(item));

        MenuInstructions instructions = OppositeArchetypes.opposite_archetypes(stream);
        return OppositeArchetypes.afterfuse(instructions);
    }

}
