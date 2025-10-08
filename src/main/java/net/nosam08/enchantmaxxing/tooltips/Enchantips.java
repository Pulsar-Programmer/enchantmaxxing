package net.nosam08.enchantmaxxing.tooltips;

import java.util.HashMap;
import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

/** Manager and creator of the Enchantment Tooltips. */
public class Enchantips {
    public static HashMap<ItemStackKey, EnchantmaxProfile> LOADED_PROFILES = new HashMap<>(); //maybe move to Client mod init?
    
    /** Starts the tooltips calculation based on the selected enchantments and their chosen levels. */
    public static void start_tooltips(ItemStack item, EnchantmaxProfile selected_enchantments){
        var key = new ItemStackKey(item);
        LOADED_PROFILES.put(key, selected_enchantments);
    }

    /** Generates the tooltips on the physical item. */
    public static void generate_tooltips(List<Text> lines, EnchantmaxProfile selected_enchantments, ItemStack stack){
        for (var ench_level : selected_enchantments.profile) {
            var name = Enchantment.getName(ench_level.enchantment, ench_level.level);
            var insert_idx = enchantment_line_idx(stack);
            lines.add(insert_idx, Text.translatable(name.getString()).withColor(EnchantifyClient.CONFIG.hoverColor));
        }
    }

    public static int enchantment_line_idx(ItemStack stack){
        int enchantmentCount = 0;
        boolean is_book = stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK;
        ItemEnchantmentsComponent enchants = stack.get(is_book ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS);
        
        if (enchants != null) {
            enchantmentCount = enchants.getSize();
        }
        
        // Insert after item name + enchantments (usually line 0 is name, then enchantments)
        int insertIndex = 1 + enchantmentCount;

        return insertIndex;
    }
}
