package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.Tests;
import net.nosam08.enchantmaxxing.emm.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.ComparePool;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;

public class OppositeArchetypes {

    /** Hashes and determines where to merge buckets from the ArchetypesInsert. */
    public static ArrayList<ArrayList<ArrayList<Enchantment>>> stc(ArchetypesInsert ai){
        //transform HashMap<A, HashSet<BC>> to Vec^2<ABC> form
        ArrayList<ArrayList<Enchantment>> bars = new ArrayList<>();
        for (var pair : ai.inner.entrySet()) {
            var key = pair.getKey();
            var value = pair.getValue();
            ArrayList<Enchantment> bar = new ArrayList<>();
            bar.add(key);
            for (Enchantment e : value) {
                bar.add(e);
            }
            bars.add(bar);
        }

        HashMap<Enchantment, Integer> stc_mapping = new HashMap<>();
        ArrayList<ArrayList<ArrayList<Enchantment>>> the_stc = new ArrayList<>();
        ArrayList<Pair<Integer, Integer>> merge_orders = new ArrayList<>();
        for (int i = 0; i < bars.size(); i++) {
            var bar = bars.get(i);
            Integer bar_focused_bucket;

            var first_atom = bar.get(0);
            //hash it
            var result = stc_mapping.get(first_atom);
            if (result == null) {
                bar_focused_bucket = the_stc.size();
                stc_mapping.put(first_atom, bar_focused_bucket);
                the_stc.add(new ArrayList<>());
            } else {
                bar_focused_bucket = result;
            }

            for (int j = 0; j < bar.size(); j++) {
                var atom = bar.get(j);

                var res = stc_mapping.get(atom);
                if (res == null) {
                    stc_mapping.put(first_atom, bar_focused_bucket);
                } else {
                    if (res == bar_focused_bucket) {
                        continue; //we don't need to rehash if they are equal since this does nothing
                    }

                    //insert merge order because these are the same buckets
                    merge_orders.add(new Pair<Integer,Integer>(bar_focused_bucket, res));
                }
            }
            the_stc.get(bar_focused_bucket).add(bar);
        }



        
        //resolve merge orders
        //keep in mind that the order in which you merge MATTERS.
        //it is guaranteed that when we merge, we can merge in reverse
        while (!merge_orders.isEmpty()) {
            var top = merge_orders.removeLast();
            var from = Math.max(top.getLeft(), top.getRight());
            var to = Math.min(top.getLeft(), top.getRight());
            //move them
            var from_section = the_stc.get(from);
            var to_section = the_stc.get(to);
            to_section.addAll(from_section);
            from_section.clear();
        }

        return the_stc;
    }
    
    /** Uses the opposite archetypes method of creating a menu. */
    public static ArrayList<BucketGroup> opposite_archetypes(ArrayList<ArrayList<ArrayList<Enchantment>>> stc){
        ArrayList<BucketGroup> built = new ArrayList<>();

        Tests.printNestedCollections(stc);

        for (var gate : stc) {
            var iter = gate.iterator();

            ArrayList<Enchantment> first;
            try {
                first = iter.next();
            } catch (Exception e) {
                continue;
            }

            var key = first.removeFirst();

            var ref_built_bg = BucketGroup.from_insert(key, first);
            
            built.add(ref_built_bg);

            while (iter.hasNext()) {
                var pair = iter.next();
                var pivot = pair.removeFirst();
                var rest = pair;

                fuse(ref_built_bg, pivot, rest);

                // merge(built, pivot, new ArrayList<>(rest));
            }
        }
        
        return built;
    }

    // /** From the ArchetypesInsert, it adds the next element to the ArchetypesPool or Vec<BucketGroup>. */
    // public static void merge(ArrayList<BucketGroup> built, Enchantment pivot, ArrayList<Enchantment> rest){
    //     if(!built.stream().anyMatch(x -> fuse(x, pivot, rest))){
    //         ///If we could not ever fuse, register independently.
    //         built.add(BucketGroup.from_insert(pivot, rest));
    //     }
    // }

    // public static String enchantment_key(Enchantment enchantment){
    //     return enchantment.description();
    // }

    /** Places the, if newfound, Buckets in the BucketGroup. */
    public static boolean fuse(BucketGroup fuse, Enchantment pivot, ArrayList<Enchantment> rest){
        ///Find repivot.
        Optional<Integer> repivot_idx = Optional.empty();
        if(!fuse.generally_contains(pivot)){
            for(var i = 0; i < rest.size(); i++){
                if(fuse.generally_contains(rest.get(i))){
                    repivot_idx = Optional.of(i);
                    break;
                }
            }
            if(!repivot_idx.isPresent()){
                return false;
            }
        }

        ///Perform the repivot minifuse if needed.
        if(repivot_idx.isPresent()){
            var idx = repivot_idx.get();

            ///Repivot minifuse.
            fuse.minifuse(rest.get(idx), pivot);

            rest.remove(idx.intValue());
        }

        for (Enchantment enchantment : rest) {
            ///Report the minifuse.
            fuse.minifuse(pivot, enchantment);
        }

        return true;
    }







    


    /** Uses the afterfuse mechanic to simplify/complexify the construction of the Menu. */
    public static MenuInstructions afterfuse(ArrayList<BucketGroup> instructions){
        MenuInstructions built = new MenuInstructions();
        for (BucketGroup bucketGroup : instructions) {
            var mutated_bucketgroup = bucketGroup.to_vec();
            
            var len = mutated_bucketgroup.size();
            
            ArrayList<Integer> indices = ensure_length(len, 0);
            ArrayList<ArrayList<Pair<Enchantment, Integer>>> row = ensure_length_new(len);
            




            var cmp_pool = new ComparePool();
            cmp_pool.load(indices, mutated_bucketgroup);

            while(!cmp_pool.is_empty()){
                var lowest = cmp_pool.get_smallest();
                var bucket_idx = lowest.get(0);
                Enchantment letter = mutated_bucketgroup.get(bucket_idx).get(indices.get(bucket_idx));
                for (Integer index : lowest) {
                    ///Increment the lowest indices. (Move past the smaller items and onto the next.)
                    indices.set(index, indices.get(index) + 1);
                } //TODO small error with what is fed to afterfuse
                afterfuse_register(lowest, letter, row);
                
                ///Reload the pool.
                cmp_pool.load(indices, mutated_bucketgroup);
            }
            built.rows.add(row);
        }

        return built;
    }



    /** Register the items that we've moved past (if multiple, we fuse them.) */
    public static void afterfuse_register(
        ArrayList<Integer> smallest, 
        Enchantment letter, 
        ArrayList<ArrayList<Pair<Enchantment, Integer>>> registry
    ){
        Optional<Integer> original_idx = Optional.empty();
        int chain = 0;

        for(Integer idx : smallest){
            if(chain == 0){
                original_idx = Optional.of(idx);
            }

            ///If the distance equals the chain, you are good to go.
            if(idx - original_idx.get() == chain){
                ///Then all is good and we can go to the next.
                chain++;
            } else{
                ///We push the formation to the proper entry.
                var pair = new Pair<Enchantment,Integer>(letter, chain);
                registry.get(original_idx.get()).add(pair);
                ///Reset the chain.
                chain = 0;
            }
        }
        ///We push the formation to the proper entry.
        var pair = new Pair<Enchantment,Integer>(letter, chain);
        registry.get(original_idx.get()).add(pair);


    }


    /** Creates n of a default item. */
    public static <T> ArrayList<T> ensure_length(int len, T def){
        var build = new ArrayList<T>();
        for(var _i = 0; _i < len; _i++) build.add(def);
        return build;
    }

    /** Creates n of a default item, cloned. */
    public static <T> ArrayList<ArrayList<T>> ensure_length_new(int len){
        var build = new ArrayList<ArrayList<T>>();
        for(var _i = 0; _i < len; _i++) build.add(new ArrayList<T>());
        return build;
    }

    

}
