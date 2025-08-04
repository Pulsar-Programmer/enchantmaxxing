package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.Optional;

import net.minecraft.enchantment.Enchantment;


/** CMP Pool of items. */
public class ComparePool {
    ArrayList<Optional<Enchantment>> cmp_pool;

    public ComparePool() {
        cmp_pool = new ArrayList<>();
    }

    /** Determines whether there are non-optional entries in the set. */
    public boolean is_empty(){

        for (var elem : cmp_pool) {
            if(elem.isPresent()) return false;
        }

        return true;
    }

    /** Restocks the ComparePool with elements from the modified BucketGroup and a set of indices. */
    public void load(ArrayList<Integer> indices, ArrayList<ArrayList<Enchantment>> bucketg){
        //god JAVA you are killing me - why can't you just be more like Rust and have type aliases?
        cmp_pool = new ArrayList<>();
        for(var i = 0; i < bucketg.size(); i++){
            var bucket = bucketg.get(i);
            var index = indices.get(i);

            cmp_pool.add(try_get(bucket, index));
        }
    }

    /** Attempts to get an element from a vec, otherwise returning empty. */
    public static <T> Optional<T> try_get(ArrayList<T> from, Integer idx){
        try {
            return Optional.of(from.get(idx));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /** Gets the smallest(s) of the items and returns the idices of them in the pool. */
    public ArrayList<Integer> get_smallest(){
        var indices = new ArrayList<Integer>();
        Optional<Integer> n = Optional.empty();

        for(var i = 0; i < cmp_pool.size(); i++) {
            var elem = cmp_pool.get(i);

            if(!elem.isPresent()) continue;
            var id = id(elem.get());

            if(!n.isPresent() || id < n.get()){
                n = Optional.of(id);
                indices.clear();
                indices.add(i);
            } else if(id == n.get()){
                indices.add(i);
            }
        }

        return indices;
    }

    /** Returns the ID of the Enchantment used for comparing. */
    public static int id(Enchantment enchantment){
        return enchantment.hashCode();
    }

    //return enchantment.toString().compareTo("");
}
