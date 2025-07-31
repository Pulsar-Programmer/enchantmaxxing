package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;

import com.google.common.base.Optional;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;

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
        Optional<Integer> repivot_idx = Optional.absent();
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
            var len = bucketGroup.inner.size();

            ArrayList<ArrayList<Pair<Enchantment, Integer>>> row = new ArrayList<>();
            
            ArrayList<Integer> indices = MenuInstructions.n_zeroes(len); //do we need this?

            //while there still exist items in the flowing stacks (or like some sort of CMP Pool of Optionals)
            //then increment the lowest one until the compare pool has a match between some of the options (only allowed lowest options)
            //TODO

            //maybe optionals > stacks so we can get the exact info and reload into the instructions after each while
            

        }





        return built; //TODO
    }

}
