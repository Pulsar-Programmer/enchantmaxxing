package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.block.FurnaceBlock;
import net.minecraft.block.entity.FurnaceBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.ComparePool;

public class OppositeArchetypes {
    
    /** Uses the opposite archetypes method of creating a menu. */
    public static ArrayList<BucketGroup> opposite_archetypes(ArchetypesInsert insert){
        ArrayList<BucketGroup> built = new ArrayList<>();

        var stream = insert.inner.entrySet().stream();

        var firstElement = stream.findFirst();
        var first = firstElement.get();
        if(firstElement.isEmpty()) return new ArrayList<>();
        built.add(BucketGroup.from_insert(first.getKey(), first.getValue()));

        stream.skip(1);

        stream.forEach(pair -> {
            var pivot = pair.getKey();
            var rest = pair.getValue();

            merge(built, pivot, rest);
        });
        
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
            
            ArrayList<Integer> indices = MenuInstructions.n_zeroes(len);
            ArrayList<ArrayList<Pair<Enchantment, Integer>>> row = new ArrayList<>();
            



            
            var cmp_pool = new ComparePool();
            cmp_pool.load(indices, mutated_bucketgroup);

            while(!cmp_pool.is_empty()){
                var lowest = cmp_pool.get_smallest();
                var bucket_idx = lowest.get(0);
                Enchantment letter = mutated_bucketgroup.get(bucket_idx).get(indices.get(bucket_idx));
                for (Integer index : lowest) {
                    ///Increment the lowest indices. (Move past the smaller items and onto the next.)
                    indices.set(index, indices.get(index) + 1);
                }
                ///Register the items that we've moved past (if multiple, we fuse them.)
                afterfuse_register(lowest, letter, row);
                
                ///Reload the pool.
                cmp_pool.load(indices, mutated_bucketgroup);
            }
        }

        return built;
    }



    /** Register the items that we've moved past (if multiple, we fuse them.) */
    public static void afterfuse_register(
        ArrayList<Integer> smallest, 
        Enchantment letter, 
        ArrayList<ArrayList<Pair<Enchantment, Integer>>> registry
    ){
        //..
        //TODO


    }

    

}
