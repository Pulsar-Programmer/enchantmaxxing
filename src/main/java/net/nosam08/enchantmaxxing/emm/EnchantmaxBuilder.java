package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.config.CurseOrderOptions;
import net.nosam08.enchantmaxxing.emm.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;

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
        ItemEnchantmentsComponent stored_enchants;

        var is_book = is_book(item);
        if(is_book){
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false);

            entries = stream.map((Enchantment x) ->enchantments.getEntry(x));

            stored_enchants = item.get(DataComponentTypes.STORED_ENCHANTMENTS);
            
            if(stored_enchants != null && !EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(stored_enchants, ench_i));
            }

        } else {
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter(ench_i -> ench_i.isSupportedItem(item));

            entries = stream.map((Enchantment x) ->enchantments.getEntry(x));

            stored_enchants = item.getEnchantments();

            if(!EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(stored_enchants, ench_i));
            }
        }

        if(EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.OFF)){
            entries = entries.filter(ench -> !ench.isIn(EnchantmentTags.CURSE));
        }

        var insert = build_from_start(entries, item, is_book, (RegistryEntry<Enchantment> e) -> {
            if(stored_enchants == null) return false;
            return stored_enchants.getEnchantments().contains(e);
        });
        
        var instructions = OppositeArchetypes.opposite_archetypes(insert);
        return instructions;
    }

    /** Determines whether the <code>ItemStack</code> is a book or enchanted book. */
    public static boolean is_book(ItemStack stack){
        return stack.isOf(Items.BOOK) || stack.isOf(Items.ENCHANTED_BOOK);
    }

    /** Checks whether an enchantment, "in an anvil", can be applied to the item. */
    public static boolean is_compatible(ItemEnchantmentsComponent enchants, RegistryEntry<Enchantment> enchantment){
        for (var ench_x : enchants.getEnchantments()) {
            var ench_x_val = ench_x.value();
            var id_ench_x = ench_x.getIdAsString();
            if(id_ench_x.equals(enchantment.getIdAsString())){
                ///Add the leveling feature. You can't change your enchantments but you sure can level one of them up.
                if(enchants.getLevel(ench_x) != ench_x_val.getMaxLevel()){
                    return true;
                }
                return false;
            }

            ///Do not display if it is blocked by one of the enchantments on the item.
            //TODO enchantments are not properly blocked
            var temp = all_enchantments();
            // EnchantifyClient.GLOBAL_ARCHETYPES.exclusive_set(ench_x_val).stream().forEach(x->System.out.println("1: " +x));
            // ench_x_val.exclusiveSet().stream().forEach(x->System.out.println("2:" + x));
            for (var ench_y : EnchantifyClient.GLOBAL_ARCHETYPES.exclusive_set(ench_x_val)) {
                var id_ench_y = temp.getEntry(ench_y).getIdAsString();
                if(id_ench_y.equals(enchantment.getIdAsString())){ //just check if it contains the enchantment instead
                    System.out.println("Y: %s, E: %s".formatted(id_ench_y, enchantment));
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
    public static ArchetypesInsert build_from_start(Stream<RegistryEntry<Enchantment>> all, ItemStack item, boolean is_book, Function<RegistryEntry<Enchantment>, Boolean> is_leveled){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            //TODO everything HERE is already filtered
            ///Do not include in exclusive set if its head enchantment is leveled.
            ///Do not enable exclusive sets if it is leveled.
            if(is_leveled.apply(ench)){
                return;
            }
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
                //TODO we must fix the riptide problem
                // built.oa_insert(val, ench.value()); ///Inserts the opposite just in case of the Riptide problem.
            });
        });

        return built;
    }

    /** Creates and returns the Global Archetypes Arrangement. */
    public static ArchetypesInsert global_archetypes(Stream<RegistryEntry<Enchantment>> all){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {

            ench.value().exclusiveSet().forEach(x -> {

                var val = x.value();
                
                ///Only let non-same archetypes through.
                if(ench.getIdAsString().equals(x.getIdAsString())){
                    return;
                }

                built.oa_insert(ench.value(), val);
                built.oa_insert(val, ench.value()); ///Inserts the opposite just in case of the Riptide problem.
            });
        });

        return built;
    }

    /** Returns the map of levels and enchantments from an item. */
    public static HashMap<Identifier, Integer> levels_map(ItemStack item){
        var reg = all_enchantments(); //TODO
        var map = new HashMap<Identifier, Integer>();

        if(is_book(item)){
            ItemEnchantmentsComponent stored_enchantments = item.get(DataComponentTypes.STORED_ENCHANTMENTS);
            if(stored_enchantments == null) return map;
            for (var ench : stored_enchantments.getEnchantments()) {
                var id = reg.getId(ench.value());
                var lvl = stored_enchantments.getLevel(ench);
                map.put(id, lvl);
            }
        } else {
            var enchs = item.getEnchantments();
            for (var ench : enchs.getEnchantments()) {
                var id = reg.getId(ench.value());
                var lvl = enchs.getLevel(ench);
                map.put(id, lvl);
            }
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
