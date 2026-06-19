package net.nosam08.enchantmaxxing.tooltips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
        net.nosam08.enchantmaxxing.profiles.ProfileStore.save();
    }

    /** Generates the tooltips on the physical item. */
    public static void generate_tooltips(List<Text> lines, EnchantmaxProfile selected_enchantments, ItemStack stack){
        for (var ench_level : selected_enchantments.profile) {
            var name = Enchantment.getName(ench_level.enchantment(), ench_level.level());
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

    /** Returns the stack's enchantments from the correct component: books keep theirs in
     * STORED_ENCHANTMENTS, everything else in ENCHANTMENTS. Using {@code getEnchantments()}
     * directly returns empty for books, which made every book hash to the same task key. */
    public static ItemEnchantmentsComponent effective_enchantments(ItemStack stack){
        boolean is_book = stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK;
        ItemEnchantmentsComponent data = stack.get(is_book
            ? DataComponentTypes.STORED_ENCHANTMENTS
            : DataComponentTypes.ENCHANTMENTS);
        return data != null ? data : ItemEnchantmentsComponent.DEFAULT;
    }

    /** Computes the enchantments gained between two stacks (new or upgraded levels). */
    public static ArrayList<EnchantmentLevelEntry> added_enchantments(ItemStack before, ItemStack after){
        var before_data = effective_enchantments(before);
        var after_data = effective_enchantments(after);
        ArrayList<EnchantmentLevelEntry> added = new ArrayList<>();
        for (var new_ench : after_data.getEnchantments()) {
            int previous_level = before_data.getLevel(new_ench);
            int now_level = after_data.getLevel(new_ench);
            if(now_level > previous_level){
                added.add(new EnchantmentLevelEntry(new_ench, now_level));
            }
        }
        return added;
    }

    /** Shifts the active task by removing what was combined with it. */
    public static void shift_active_task(ItemStack item, ArrayList<EnchantmentLevelEntry> enchantments, ItemStack result){
        var key = new ItemStackKey(item);
        var task = ACTIVE_TASKS.remove(key);
        // key.read(); System.out.println("Old key!");
        // if(task == null) System.out.println("When shifting tasks, ORIGINAL is null!");
        if(task == null) return;
        for (EnchantmentLevelEntry entry : enchantments) {
            task.profile.removeIf((EnchantmentLevelEntry profile_entry) -> {
                if(!entry.enchantment().getIdAsString().equals(profile_entry.enchantment().getIdAsString())) return false;
                if(entry.level() >= profile_entry.level()){
                    //then override
                    return true;
                }
                return false;
            });
        }
        // task.profile.forEach((var x) -> System.out.println("current task:" + x.enchantment.getIdAsString() + " " + x.level)); //need to consider WP for Keys
        // item.addEnchantment(enchantment.enchantment, enchantment.level);
        if(task.profile.isEmpty()){ //task hit zero: drop it instead of re-adding
            net.nosam08.enchantmaxxing.profiles.ProfileStore.save();
            return;
        }
        var new_key = new ItemStackKey(result);
        // new_key.read(); System.out.println("New key!");
        ACTIVE_TASKS.put(new_key, task);
        net.nosam08.enchantmaxxing.profiles.ProfileStore.save();
    }

    /** Grinding strips every (non-curse) enchantment off the item, so its active task no
     * longer applies: drop the task entirely. */
    public static void grindstone_task(ItemStack item){
        var key = new ItemStackKey(item);
        if (ACTIVE_TASKS.remove(key) == null) return;
        net.nosam08.enchantmaxxing.profiles.ProfileStore.save();
    }







    /**
     * Returns a copy of the given ItemStack with all enchantments removed, but WP maintained.
     */
    public static ItemStack stripEnchantments(ItemStack stack) {
        ItemStack stripped = new ItemStack(stack.getItem(), stack.getCount());
        if (stack.contains(DataComponentTypes.REPAIR_COST)) {
            stripped.set(DataComponentTypes.REPAIR_COST, stack.get(DataComponentTypes.REPAIR_COST));
        }
        return stripped;
    }

    /** Removes the modded orange stuff and all other data except the Enchantment data from the ItemStack. Maintains WP. */
    public static ItemStack strip_extras(ItemStack stack) {
        ItemStack stripped = stripEnchantments(stack);
        // Build a fresh enchantments component instead of referencing the original. Read from the
        // correct source (STORED_ENCHANTMENTS for books) so each book gets a distinct key, and write
        // it back to the same component so the item still renders/tooltips its enchantments.
        boolean is_book = stack.getItem() == Items.ENCHANTED_BOOK || stack.getItem() == Items.BOOK;
        ItemEnchantmentsComponent source = effective_enchantments(stack);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
        source.getEnchantments().forEach(entry ->
            builder.add(entry, source.getLevel(entry))
        );
        stripped.set(is_book ? DataComponentTypes.STORED_ENCHANTMENTS : DataComponentTypes.ENCHANTMENTS, builder.build());
        if (stack.contains(DataComponentTypes.CUSTOM_NAME)) {
            stripped.set(DataComponentTypes.CUSTOM_NAME, stack.get(DataComponentTypes.CUSTOM_NAME));
        }
        // stripped.remove(DataComponentTypes.CUSTOM_DATA); // wipes mod NBT
        return stripped;
    }
}
