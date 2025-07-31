package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.Optional;


/** CMP Pool of items. */
public class ComparePool {
    ArrayList<Optional<Bucket>> cmp_pool;

    

    public ComparePool() {
        cmp_pool = new ArrayList<>();
    }


    public void restock(ArrayList<Integer> indices, BucketGroup bucketg){
        cmp_pool = new ArrayList<>();
        for (Bucket bucket : bucketg.inner) {
            // cmp_pool.add(bucket.inner.)
        }
        //TODO - you cannot do .get() at a position for bucket... yikes

    }
    
    
    public ArrayList<Integer> get_smallest(){
        var indices = new ArrayList<Integer>();
        var n = Optional.empty();

        for(var i = 0; i < cmp_pool.size(); i++) {
            
        }

        //TODO


        return indices;
    }

    
    //find index of lowest elem
}
