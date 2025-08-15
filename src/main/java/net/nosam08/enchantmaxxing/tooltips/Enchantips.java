package net.nosam08.enchantmaxxing.tooltips;

import java.util.HashMap;
import java.util.List;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.Enchantify;
import net.nosam08.enchantmaxxing.menu.component_data.EnchantmentButton;

/** Manager and creator of the Enchantment Tooltips. */
public class Enchantips {
    
    
    /** Starts the tooltips calculation based on the selected enchantments and their chosen levels. */
    public static void start_tooltips(ItemStack item, HashMap<Integer, Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>>> selected_enchantments){
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (ItemStack.areItemsAndComponentsEqual(stack, item)) {
                generate_tooltips(lines, selected_enchantments);
            }
        });
    }

    public static void generate_tooltips(List<Text> lines, HashMap<Integer, Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>>> selected_enchantments){
        for (Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>> bucket_groups : selected_enchantments.values()) {
            for (var hm_entry : bucket_groups.getRight().entrySet()) {
                var ench = hm_entry.getKey();
                var level = hm_entry.getValue().getLeft();
                var name = Enchantment.getName(ench, level);
                lines.add(Text.translatable(name.getString()).withColor(Enchantify.CONFIG.hoverColor));
            }
        }
    }



}
