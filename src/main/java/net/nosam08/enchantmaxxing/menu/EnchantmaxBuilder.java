package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {

    /** Returns all enchantments. */
    public static Registry<Enchantment> all_enchantments(){
        return MinecraftClient.getInstance()
            .getNetworkHandler()
            .getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    /** Builds the core and direct menu instructions given the item to Enchantmax. */
    public static ArrayList<BucketGroup> build_direct(ItemStack item){

        Registry<Enchantment> enchantments;
        try {
            enchantments = all_enchantments();
        } catch (Exception e) {
            return new ArrayList<>();
        }

        Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter(ench_i -> ench_i.isSupportedItem(item));

        Stream<RegistryEntry<Enchantment>> entries = stream.map((Enchantment x) ->enchantments.getEntry(x));

        if(!EnchantifyClient.CONFIG.is_static){
            entries = entries.filter(ench_i -> is_compatible(item, ench_i));
        }

        if(EnchantifyClient.CONFIG.curse_order.equals("OFF")){
            entries = entries.filter(ench -> !ench.isIn(EnchantmentTags.CURSE));
        }

        var insert = build_from_start(entries, item);

        // System.out.println(insert.display());
        
        var instructions = OppositeArchetypes.opposite_archetypes(insert);
        return instructions;
    }

    /** Checks whether an enchantment, "in an anvil", can be applied to the item. */
    public static boolean is_compatible(ItemStack item, RegistryEntry<Enchantment> enchantment){
        for (var ench_x : item.getEnchantments().getEnchantments()) {
            var ench_x_val = ench_x.value();
            var id_ench_x = ench_x.getIdAsString();
            if(id_ench_x.equals(enchantment.getIdAsString())){
                ///Add the leveling feature. You can't change your enchantments but you sure can level one of them up.
                // TODO - what if there are two like the trident tho?
                // if(item.getEnchantments().getLevel(ench_x) != ench_x_val.getMaxLevel()){
                //     return true;
                // }
                return false;
            }

            for (var ench_y : ench_x_val.exclusiveSet()) {
                var id_ench_y = ench_y.getIdAsString();
                if(id_ench_y.equals(enchantment.getIdAsString())){
                        return false;
                }
            }
        }

        return true;
    }
    
    /** Builds the menu appearance given the item to Enchantmax. */
    public static MenuInstructions build_afterfuse(ItemStack item){
        var instructions = build_direct(item);
        return OppositeArchetypes.afterfuse(instructions);
    }

    /** Builds the ArchetypesInsert from a stream. */
    public static ArchetypesInsert build_from_start(Stream<RegistryEntry<Enchantment>> all, ItemStack item){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            built.prepare(ench.value());
            ench.value().exclusiveSet().forEach(x -> {

                var val = x.value();
                
                ///Only let supported and non-same archetypes through.
                if(ench.getIdAsString().equals(x.getIdAsString()) || !val.isSupportedItem(item)){
                    return;
                }

                ///Do not include if an enchantment is a curse and they are off.
                if(EnchantifyClient.CONFIG.curse_order.equals("OFF") && x.isIn(EnchantmentTags.CURSE)){
                    return;
                }

                built.oa_insert(ench.value(), val);
            });
        });

        return built;
    }

    // public static boolean is_supported(Enchantment val, ItemStack item){
    //     System.out.println(item.getItem().getTranslationKey());
    //     return item.getItem().getTranslationKey().equals("item.minecraft.book") || val.isSupportedItem(item);
    // }

    public static HashMap<Identifier, Integer> levels_map(ItemStack item){
        var reg = all_enchantments();
        var map = new HashMap<Identifier, Integer>();
        var enchs = item.getEnchantments();
        for (var ench : enchs.getEnchantments()) {
            var id = reg.getId(ench.value());
            var lvl = enchs.getLevel(ench);
            map.put(id, lvl);
        }
        return map;
    }
}
