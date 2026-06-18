package net.nosam08.enchantmaxxing.aom.actors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
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
    /**
     * Cache of solved orders, keyed by a content {@link #signature}. Previously keyed by
     * {@code Pair<ItemStackKey, EnchantmaxProfile>} — but {@code net.minecraft.util.Pair} has no
     * {@code equals}/{@code hashCode}, so every lookup missed and the (exponential) solve reran on
     * every menu open. A value-based string key makes it actually hit, and lets the solved order
     * be persisted and reloaded across restarts (see {@link #seed}/{@link #peek}).
     */
    public static HashMap<String, Pair<String, Integer>> STORE = new HashMap<>();

    /** Deterministic, restart-stable key for an (item, profile) pair: item id + base work
     * penalty + the sorted profile. The solved cost/order depend only on these. */
    public static String signature(ItemStackKey item, EnchantmaxProfile enchantments){
        var stack = item.inner();
        var pwp_item = stack.get(DataComponentTypes.REPAIR_COST);
        int base_pwp = pwp_item != null ? pwp_item : 0;

        var entries = enchantments.profile.stream()
            .map(AnvilOrdering::serialize_enchantment)
            .sorted()
            .collect(Collectors.joining(","));

        return Registries.ITEM.getId(stack.getItem()) + "|" + base_pwp + "|" + entries;
    }

    /** Returns the already-solved (order, cost) for this task, or null if not cached yet. */
    public static Pair<String, Integer> peek(ItemStackKey item, EnchantmaxProfile enchantments){
        return STORE.get(signature(item, enchantments));
    }

    /** Pre-populates the cache with a previously-solved order (used when loading from disk). */
    public static void seed(ItemStackKey item, EnchantmaxProfile enchantments, String order, int cost){
        STORE.put(signature(item, enchantments), new Pair<>(order, cost));
    }

    public static OrderString ordering(ItemStackKey item, EnchantmaxProfile enchantments){
        var key = signature(item, enchantments);
        var result = STORE.get(key);
        if (result == null) {
            var pwp_item = item.inner().get(DataComponentTypes.REPAIR_COST);
            // System.out.println("PWP ITEM: " + pwp_item);
            var n_set = n_set(new SimItem(pwp_item, "OBJ"), enchantments.profile.stream().map((x)->SimEnchantment.from_enchantment(x)).collect(Collectors.toCollection(ArrayList::new)));
            var paths = parse_paths(n_set);
            result = obtain_ordered(paths);



            // int base_pwp = pwp_item != null ? pwp_item : 0;
            // var e = enchantments.profile.stream()
            //     .map(SimEnchantment::from_enchantment)
            //     .collect(Collectors.toCollection(ArrayList::new));
            // result = solve(new SimItem(base_pwp, "OBJ"), e);
            STORE.put(key, result);
        }
        return new OrderString(item.inner(), result.getLeft(), result.getRight());
    }


    public static Pair<String, Integer> obtain_ordered(ArrayList<Pair<String, Integer>> paths){
        // paths.forEach((x)->System.out.println(x.getLeft() + x.getRight()));
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

            path.open(n_set(new_o.clone(), new_e));
            head.open(path);
        }
        //CHOOSE
        for(var i = 0; i < e.size(); i++){
            var new_e = clone(e);

            var first = new_e.remove(i).clone();
            for(var j=0; j < new_e.size();j++){
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
