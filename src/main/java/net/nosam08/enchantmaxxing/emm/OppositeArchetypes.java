package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Optional;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.emm.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.ComparePool;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;

public class OppositeArchetypes {
    
    /** Uses the opposite archetypes method of creating a menu. */
    public static ArrayList<BucketGroup> opposite_archetypes(ArchetypesInsert insert){
        ArrayList<BucketGroup> built = new ArrayList<>();

        var iter = insert.inner.entrySet().iterator();

        Entry<Enchantment, ArrayList<Enchantment>> first;
        try {
            first = iter.next();
        } catch (Exception e) {
            return new ArrayList<>();
        }
        
        built.add(BucketGroup.from_insert(first.getKey(), first.getValue()));

        while (iter.hasNext()) {
            var pair = iter.next();
            var pivot = pair.getKey();
            var rest = pair.getValue();

            merge(built, pivot, rest);
        }
        
        return built;
    }

    /** From the ArchetypesInsert, it adds the next element to the ArchetypesPool or Vec<BucketGroup>. */
    public static void merge(ArrayList<BucketGroup> built, Enchantment pivot, ArrayList<Enchantment> rest){
        if(!built.stream().anyMatch(x -> fuse(x, pivot, rest))){
            ///If we could not ever fuse, register independently.
            built.add(BucketGroup.from_insert(pivot, rest));
        }
    }

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
