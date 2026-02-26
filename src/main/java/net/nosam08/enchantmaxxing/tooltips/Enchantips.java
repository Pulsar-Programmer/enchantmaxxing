package net.nosam08.enchantmaxxing.tooltips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

/** Manager and creator of the Enchantment Tooltips. */
public class Enchantips {
    public static HashMap<ItemStackKey, EnchantmaxProfile> ACTIVE_TASKS = new HashMap<>(); //maybe move to Client mod init?
    
    /** Starts the tooltips calculation based on the selected enchantments and their chosen levels. */
    public static void start_tooltips(ItemStack item, EnchantmaxProfile selected_enchantments){
        var key = new ItemStackKey(item);
        ACTIVE_TASKS.put(key, selected_enchantments);
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

    /** Shifts the active task by removing what was combined with it. */ //< do we add one of these for grindstone?
    public static void shift_active_task(ItemStack item, ArrayList<EnchantmentLevelEntry> enchantments, ItemStack result){
        var key = new ItemStackKey(item);
        var task = ACTIVE_TASKS.remove(key);
        if(task == null) System.out.println("When shifting tasks, ORIGINAL is null!");
        if(task == null) return;
        task.profile.removeIf((EnchantmentLevelEntry x) -> enchantments.contains(x));
        task.profile.forEach((var x) -> System.out.println("current task:" + x.enchantment.getIdAsString() + " " + x.level));
        // item.addEnchantment(enchantment.enchantment, enchantment.level);
        var new_key = new ItemStackKey(result);
        ACTIVE_TASKS.put(new_key, task);
    }







    /**
     * Returns a copy of the given ItemStack with all enchantments removed.
     */
    public static ItemStack stripEnchantments(ItemStack stack) {
        ItemStack stripped = new ItemStack(stack.getItem(), stack.getCount());
        return stripped;
    }

    /** Removes the modded orange stuff and all other data except the Enchantment data from the ItemStack. Clears also WP which is useful for after enchantment shifts and stuff. Diff WP will collapse to same Key this means however. */
    public static ItemStack strip_extras(ItemStack stack) {
        ItemStack stripped = stripEnchantments(stack);
        // Build a fresh enchantments component instead of referencing the original
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        stack.getEnchantments().getEnchantments().forEach(entry -> 
            builder.add(entry, stack.getEnchantments().getLevel(entry))
        );
        stripped.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        // stripped.remove(DataComponentTypes.CUSTOM_DATA); // wipes mod NBT
        return stripped;
    }
}
