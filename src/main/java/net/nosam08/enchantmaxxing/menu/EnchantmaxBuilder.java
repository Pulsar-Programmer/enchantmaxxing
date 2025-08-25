package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.config.CurseOrderOptions;
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

        Stream<RegistryEntry<Enchantment>> entries;

        var is_book = is_book(item);
        if(is_book){
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false);

            entries = stream.map((Enchantment x) ->enchantments.getEntry(x));

            ItemEnchantmentsComponent stored_enchantments = item.get(DataComponentTypes.STORED_ENCHANTMENTS);
            
            if(stored_enchantments != null && !EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(stored_enchantments.getEnchantments(), ench_i));
            }
        } else {
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter(ench_i -> ench_i.isSupportedItem(item));

            entries = stream.map((Enchantment x) ->enchantments.getEntry(x));

            var enchants = item.getEnchantments().getEnchantments();

            if(!EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(enchants, ench_i));
            }
        }

        if(EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.OFF)){
            entries = entries.filter(ench -> !ench.isIn(EnchantmentTags.CURSE));
        }

        var insert = build_from_start(entries, item, is_book);

        // System.out.println(insert.display());
        
        var instructions = OppositeArchetypes.opposite_archetypes(insert);
        return instructions;
    }

    /** Determines whether the <code>ItemStack</code> is a book or enchanted book. */
    public static boolean is_book(ItemStack stack){
        return stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    /** Checks whether an enchantment, "in an anvil", can be applied to the item. */
    public static boolean is_compatible(Set<RegistryEntry<Enchantment>> enchants, RegistryEntry<Enchantment> enchantment){
        for (var ench_x : enchants) {
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
    public static ArchetypesInsert build_from_start(Stream<RegistryEntry<Enchantment>> all, ItemStack item, boolean is_book){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            built.prepare(ench.value());
            ench.value().exclusiveSet().forEach(x -> {

                var val = x.value();
                
                ///Only let non-same archetypes through.
                if(ench.getIdAsString().equals(x.getIdAsString())){
                    return;
                }

                ///Only let supported archetypes through.
                if(!(is_book || val.isSupportedItem(item))){
                    return;
                }

                ///Do not include if an enchantment is a curse and they are off.
                if(EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.OFF) && x.isIn(EnchantmentTags.CURSE)){
                    return;
                }

                built.oa_insert(ench.value(), val);
            });
        });

        return built;
    }

    public static HashMap<Identifier, Integer> levels_map(ItemStack item){
        var reg = all_enchantments(); //TODO
        var map = new HashMap<Identifier, Integer>();
        var enchs = item.getEnchantments();
        for (var ench : enchs.getEnchantments()) {
            var id = reg.getId(ench.value());
            var lvl = enchs.getLevel(ench);
            map.put(id, lvl);
        }
        return map;
    }

    /** Takes the ArrayList<BucketGroup> and sorts the ones containing curses to the bottom.*/
    public static ArrayList<BucketGroup> to_vec_curses(ArrayList<BucketGroup> bgs, Registry<Enchantment> reg){
        var first = EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.TOP) ? 1 : 0;
        var second = EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.BOTTOM) ? 2 : 0;
        var num = first + second;

        if(num == 0){
            return bgs;
        }
        
        var main = new ArrayList<BucketGroup>();
        var curses = new ArrayList<BucketGroup>();
        var singleton_curses = new ArrayList<BucketGroup>();
        for (BucketGroup enchantment : bgs) {
            if(enchantment.generally_contains_curse(reg)){
                if(enchantment.inner.size() == 1){
                    singleton_curses.add(enchantment);
                } else {
                    curses.add(enchantment);
                }
            } else {
                main.add(enchantment);
            }
        }

        if(num == 2){
            main.addAll(curses);
            main.addAll(singleton_curses);
            return main;
        } else {
            singleton_curses.addAll(curses);
            singleton_curses.addAll(main);
            return singleton_curses;
        }
    }
}
