package net.nosam08.enchantmaxxing.tooltips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.Enchantify;
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
    public static void generate_tooltips(List<Text> lines, EnchantmaxProfile selected_enchantments){
        for (var ench_level : selected_enchantments.profile) {
            var name = Enchantment.getName(ench_level.enchantment, ench_level.level);
            lines.add(Text.translatable(name.getString()).withColor(Enchantify.CONFIG.hoverColor));
        }
    }
}
