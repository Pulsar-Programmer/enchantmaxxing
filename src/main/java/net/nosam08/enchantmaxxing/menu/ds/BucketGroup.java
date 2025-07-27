package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.enchantment.Enchantment;

public class BucketGroup {
    ArrayList<Bucket> inner;

    public BucketGroup(){
        inner = new ArrayList<>();
    }

    public BucketGroup(ArrayList<Bucket> inner){
        this.inner = inner;
    }

    /** Creates the first BucketGroup from the ArchetypesInsert. */
    public static BucketGroup from_insert(Enchantment pivot, ArrayList<Enchantment> rest){
        var first = new ArrayList<Enchantment>();
        first.add(pivot);

        var bucket1 = new Bucket(first);
        var bucket2 = new Bucket(rest);

        var result = new ArrayList<Bucket>();
        result.add(bucket1);
        result.add(bucket2);
        

        return new BucketGroup(result);
    }

    /** Checks across all the buckets whether an item is contained. */
    public boolean generally_contains(Enchantment e){
        for(var x: inner){
            if(x.inner.contains(e)){
                return true;
            }
        }
        return false;
    }

    /** Performs a minifuse. */
    public void minifuse(Enchantment a, Enchantment b){
        // TODO A LOT HERE
    }
}
