package net.nosam08.enchantmaxxing.aom.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;
import net.nosam08.enchantmaxxing.aom.ds.SimEnchantment;
import net.nosam08.enchantmaxxing.aom.ds.SimItem;
import net.nosam08.enchantmaxxing.aom.ds.SimReport;
import net.nosam08.enchantmaxxing.aom.ds.SimReportTree;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

public class AnvilOrdering {
    public static HashMap<Pair<ItemStackKey, EnchantmaxProfile>, Pair<String, Integer>> STORE = new HashMap<>();



    public static OrderString ordering(ItemStackKey item, EnchantmaxProfile enchantments){
        var args = new Pair<ItemStackKey,EnchantmaxProfile>(item, enchantments);
        if (STORE.get(args) == null) {
            var n_set = n_set(new SimItem(0, "OBJ"), enchantments.profile.stream().map((x)->SimEnchantment.from_enchantment(x)).collect(Collectors.toCollection(ArrayList::new)));
            var paths = parse_paths(n_set);
            var string = obtain_ordered(paths);
            STORE.put(args, string);
        }
        var result = STORE.get(args);
        return new OrderString(item.inner(), result.getLeft(), result.getRight());
    }


    public static Pair<String, Integer> obtain_ordered(ArrayList<Pair<String, Integer>> paths){
        paths.forEach((x)->System.out.println(x.getLeft() + x.getRight()));
        var lowest = new String();
        Optional<Integer> lowest_cost = Optional.empty();
        for (Pair<String,Integer> pair : paths) {
            if(lowest_cost.isEmpty() || lowest_cost.get() > pair.getRight()){
                lowest_cost = Optional.of(pair.getRight());
                lowest = pair.getLeft();
            }
        }
        return new Pair<String,Integer>(lowest, lowest_cost.get());
    }


    public static ArrayList<Pair<String, Integer>> parse_paths(SimReportTree head){
        var cost = head.current.exp_sum();
        if(head.disciples.isEmpty()){
            return new ArrayList<>(Arrays.asList(new Pair<String, Integer>(head.current.operation, cost)));
        }
        var costs = new ArrayList<Pair<String, Integer>>();
        for (SimReportTree tree : head.disciples) {
            var rest = parse_paths(tree).stream().map((pair) -> {
                return new Pair<String, Integer>(pair.getLeft(), pair.getRight() + cost);
            }).collect(Collectors.toCollection(ArrayList::new));
            costs.addAll(rest);
        }
        return costs;
    }


    public static SimReportTree one_set(SimItem o, SimEnchantment x){
        var operation = SimReport.combine(o, x);

        return new SimReportTree(operation);
    }

    /** Brute force algorithm to find n_set. */
    public static SimReportTree n_set(SimItem o, ArrayList<SimEnchantment> e){
        if(e.size() == 1){
            return one_set(o, e.getFirst());
        }

        var head = new SimReportTree(new SimReport(0, 0, 0, "HEAD"));
        //BASIC
        for(var i = 0; i < e.size(); i++){
            var new_e = clone(e);
            var new_o = o.clone();

            var path = one_set(new_o, new_e.remove(i));

            path.open(n_set(new_o, new_e));
            head.open(path);
        }
        //CHOOSE
        for(var i = 0; i < e.size(); i++){
            var new_e = clone(e);

            var first = new_e.remove(i).clone();
            for(var j=0; j < e.size()-1;j++){
                var new_e_2 = clone(new_e);

                var second = new_e_2.remove(j);

                var pair1 = SimReport.merged(first, second);
                // var pair2 = SimReport.merged(second, first);

                var path1 = new SimReportTree(pair1.getRight());
                // var path2 = new SimReportTree(pair2.getRight());

                var resolved1 = clone(new_e_2);
                // var resolved2 = clone(new_e_2);

                resolved1.add(pair1.getLeft());
                // resolved2.add(pair2.getLeft());

                path1.open(n_set(o.clone(), resolved1));
                // path2.open(n_set(o.clone(), resolved2));

                head.open(path1);
                // head.open(path2);
            }
        }

        return head;
    }

    public static ArrayList<SimEnchantment> clone(ArrayList<SimEnchantment> e){
        return e.stream().map((ench) -> ench.clone()).collect(Collectors.toCollection(ArrayList::new));
    }


    






    

    


    /** Advances the PWP of an object. */
    public static Integer adv(Integer x){
        return x * 2 + 1;
    }




    /** Creates a String from the enchantment. */
    public static String serialize_enchantment(EnchantmentLevelEntry entry){
        return entry.enchantment.getIdAsString() + ";" + entry.level;
    }

    /** Creates an ItemStack from the Enchantment. */
    public static ItemStack deserialize_enchantment(String enchantment) {
        String[] parts = enchantment.split(";");
        if (parts.length != 2) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        String enchantmentId = parts[0];
        int level = Integer.parseInt(parts[1]);
        
        Identifier id = Identifier.tryParse(enchantmentId);
        if (id == null) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        
        RegistryWrapper<Enchantment> enchantmentRegistry = EnchantmaxBuilder.all_enchantments();
        Optional<RegistryEntry.Reference<Enchantment>> enchantmentEntry = enchantmentRegistry.getOptional(
            RegistryKey.of(RegistryKeys.ENCHANTMENT, id)
        );

        book.addEnchantment(enchantmentEntry.get(), level);
        
        return book;
    }



}
