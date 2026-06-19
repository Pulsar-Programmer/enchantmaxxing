package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.resources.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.config.CurseOrderOptions;
import net.nosam08.enchantmaxxing.emm.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {

    /** Returns all enchantments. */
    public static Registry<Enchantment> all_enchantments(){
        return Minecraft.getInstance()
            .getConnection()
            .registryAccess()
            .lookupOrThrow(Registries.ENCHANTMENT);
    }

    /** Builds the core and direct menu instructions given the item to Enchantmax. */
    public static ArrayList<BucketGroup> build_direct(ItemStack item){

        Registry<Enchantment> enchantments;
        try {
            enchantments = all_enchantments();
        } catch (Exception e) {
            return new ArrayList<>();
        }

        Stream<Holder<Enchantment>> entries;
        ItemEnchantments stored_enchants;

        var is_book = is_book(item);
        if(is_book){
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false);

            entries = stream.map((Enchantment x) ->enchantments.wrapAsHolder(x));

            stored_enchants = item.get(DataComponents.STORED_ENCHANTMENTS);
            
            if(stored_enchants != null && !EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(stored_enchants, ench_i));
            }

        } else {
            Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter(ench_i -> ench_i.isSupportedItem(item));

            entries = stream.map((Enchantment x) ->enchantments.wrapAsHolder(x));

            stored_enchants = item.getEnchantments();

            if(!EnchantifyClient.CONFIG.is_static){
                entries = entries.filter(ench_i -> is_compatible(stored_enchants, ench_i));
            }
        }

        if(EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.OFF)){
            entries = entries.filter(ench -> !ench.is(EnchantmentTags.CURSE));
        }

        var insert = build_from_start(entries, item, is_book, (Holder<Enchantment> e) -> {
            if(stored_enchants == null) return false;
            return stored_enchants.keySet().contains(e);
        });
        
        var stc = OppositeArchetypes.stc(insert);
        var instructions = OppositeArchetypes.opposite_archetypes(stc);
        return instructions;
    }

    /** Determines whether the <code>ItemStack</code> is a book or enchanted book. */
    public static boolean is_book(ItemStack stack){
        return stack.getItem() == Items.BOOK || stack.getItem() == Items.ENCHANTED_BOOK;
    }

    /** Checks whether an enchantment, "in an anvil", can be applied to the item. */
    public static boolean is_compatible(ItemEnchantments enchants, Holder<Enchantment> enchantment){
        for (var ench_x : enchants.keySet()) {
            var ench_x_val = ench_x.value();
            var id_ench_x = ench_x.getRegisteredName();
            if(id_ench_x.equals(enchantment.getRegisteredName())){
                ///Add the leveling feature. You can't change your enchantments but you sure can level one of them up.
                if(enchants.getLevel(ench_x) != ench_x_val.getMaxLevel()){
                    return true;
                }
                return false;
            }

            ///Do not display if it is blocked by one of the enchantments on the item.
            ///force_combinable removes every block; otherwise GLOBAL_ARCHETYPES already reflects
            ///the runtime areCompatible rules (built per connection).
            if(!EnchantifyClient.CONFIG.force_combinable
                && EnchantifyClient.GLOBAL_ARCHETYPES.exclusive_set(ench_x_val).contains(enchantment.value())){
                return false;
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
    public static ArchetypesInsert build_from_start(Stream<Holder<Enchantment>> all, ItemStack item, boolean is_book, Function<Holder<Enchantment>, Boolean> is_leveled){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            ///We prepare here in order to register the item in the pool to begin with.
            built.prepare(ench.value());

            //TODO everything HERE is already filtered
            ///Do not include in exclusive set if its head enchantment is leveled.
            ///Do not enable exclusive sets if it is leveled.
            if(is_leveled.apply(ench)){
                return;
            }
            ench.value().exclusiveSet().forEach(x -> {

                var val = x.value();

                ///Only let non-same archetypes through.
                if(ench.getRegisteredName().equals(x.getRegisteredName())){
                    return;
                }

                ///Respect compat mods that unlocked combining this pair (see actually_exclusive).
                if(!actually_exclusive(ench, x)){
                    return;
                }

                ///Only let supported archetypes through.
                if(!(is_book || val.isSupportedItem(item))){
                    return;
                }

                ///Do not include if an enchantment is a curse and they are off.
                if(EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.OFF) && x.is(EnchantmentTags.CURSE)){
                    return;
                }

                built.oa_insert(ench.value(), val);
                built.oa_insert(val, ench.value()); ///Inserts the opposite just in case of the Riptide problem.
            });
        });

        return built;
    }

    /** Creates and returns the Global Archetypes Arrangement. */
    public static ArchetypesInsert global_archetypes(Stream<Holder<Enchantment>> all){
        var built = new ArchetypesInsert();
        
        all.forEach(ench -> {
            built.prepare(ench.value());
            ench.value().exclusiveSet().forEach(x -> {

                var val = x.value();

                ///Only let non-same archetypes through.
                if(ench.getRegisteredName().equals(x.getRegisteredName())){
                    return;
                }

                ///Respect compat mods that unlocked combining this pair (see actually_exclusive).
                if(!actually_exclusive(ench, x)){
                    return;
                }

                built.oa_insert(ench.value(), val);
                built.oa_insert(val, ench.value()); ///Inserts the opposite just in case of the Riptide problem.
            });
        });

        return built;
    }

    /**
     * Whether two enchantments really cannot be combined right now. The vanilla
     * {@code exclusiveSet()} data is only the *default*: the authoritative runtime check is
     * {@link Enchantment#areCompatible}, which compatibility mods hook to unlock things like
     * stacking every protection. We list a pair as exclusive only when the game still refuses to
     * combine them, so those compat mods are honoured automatically. {@code force_combinable}
     * is the manual override for compat mods that bypass {@code areCompatible} altogether.
     */
    public static boolean actually_exclusive(Holder<Enchantment> a, Holder<Enchantment> b){
        if(EnchantifyClient.CONFIG.force_combinable){
            return false;
        }
        return !Enchantment.areCompatible(a, b);
    }

    /** Returns the map of levels and enchantments from an item. */
    public static HashMap<Identifier, Integer> levels_map(ItemStack item){
        var reg = all_enchantments(); //TODO
        var map = new HashMap<Identifier, Integer>();

        if(is_book(item)){
            ItemEnchantments stored_enchantments = item.get(DataComponents.STORED_ENCHANTMENTS);
            if(stored_enchantments == null) return map;
            for (var ench : stored_enchantments.keySet()) {
                var id = reg.getKey(ench.value());
                var lvl = stored_enchantments.getLevel(ench);
                map.put(id, lvl);
            }
        } else {
            var enchs = item.getEnchantments();
            for (var ench : enchs.keySet()) {
                var id = reg.getKey(ench.value());
                var lvl = enchs.getLevel(ench);
                map.put(id, lvl);
            }
        }

        return map;
    }

    /** Takes the ArrayList<BucketGroup> and sorts the ones containing curses to the configured order.*/
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
