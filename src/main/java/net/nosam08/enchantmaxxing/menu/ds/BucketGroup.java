package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;

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
        ///To explain some things, A is guaranteed to be present in here by this point. B is getting roped into this mess - this brawl.
        ArrayList<Bucket> archetypes = new ArrayList<>();
        ArrayList<Bucket> opposite_archetypes = new ArrayList<>();

        ///Loop through each bucket to find where the Enchantment is/isn't.
        for (Bucket bucket : inner) {
            if(bucket.inner.contains(a)){
                archetypes.add(bucket);
            } else{
                opposite_archetypes.add(bucket);
            }
        }

        var contains_secondary = generally_contains(b);

        ///Do not do OA if B is present across the buckets. If it is present, it means it has already been roped in and considered. You only OA if it is a new secondary.
        if(!contains_secondary){
            ///OPPOSITE ARCHETYPES
            for(Bucket bucket : opposite_archetypes){
                bucket.inner.add(b);
            }
        }
        
        for (Bucket bucket : archetypes) {
            ///Substitution and the Drive Mechanic are technically the same, but they happen in different circumstances.
            
            ///Just like OA, you do not want to do substitution or LA (like-archetypes) for risk of contaminating the already-considered, delicate pool.
            ///Substitution only happens if there are no present secondaries within the entire list.
            if(!contains_secondary){
                ///SUBSTITUTION
                var clone = bucket.clone();
                clone.replace(a, b);
                inner.add(clone);
            }
            
            ///Drive always happens if the secondary is in the same bucket as the primary.
            if(bucket.inner.contains(b)){
                ///Clear
                bucket.inner.remove(b);
                ///DRIVE MECHANIC
                var clone = bucket.clone();
                clone.replace(a, b);
                inner.add(clone);
            }
        }

        
        
        //TODO do not forget to check for subsets (subset buckets to the left and right or just any generally new ones or OA)
    }


}
