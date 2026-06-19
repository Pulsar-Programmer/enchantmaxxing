package net.nosam08.enchantmaxxing.emm.ds;

import java.util.ArrayList;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Registry;
import net.minecraft.tags.EnchantmentTags;

public class BucketGroup {
    public ArrayList<Bucket> inner;

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
        if(!rest.isEmpty()){
            result.add(bucket2);
        }
        

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

    /** Checks across all the buckets whether a curse is contained. */
    public boolean generally_contains_curse(Registry<Enchantment> reg){
        for(var x: inner){
            for(var y: x.inner){
                if(reg.wrapAsHolder(y).is(EnchantmentTags.CURSE)) return true;
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
        
        ///We create a new list of like archetypes to house the newly created elements, later adding them to the Group.
        ArrayList<Bucket> new_archetypes = new ArrayList<>();

        for (Bucket bucket : archetypes) {
            ///Substitution and the Drive Mechanic are technically the same, but they happen in different circumstances.
            
            ///Just like OA, you do not want to do substitution or LA (like-archetypes) for risk of contaminating the already-considered, delicate pool.
            ///Substitution only happens if there are no present secondaries within the entire list.
            if(!contains_secondary){
                ///Consider the first bucket part of new_archetypes.
                inner.remove(bucket);
                new_archetypes.add(bucket);
                ///SUBSTITUTION
                var clone = bucket.clone();
                clone.replace(a, b);
                new_archetypes.add(clone);
            }
            
            ///Drive always happens if the secondary is in the same bucket as the primary.
            if(bucket.inner.contains(b)){
                ///Consider the first bucket part of new_archetypes.
                inner.remove(bucket);
                new_archetypes.add(bucket);
                ///Clear
                bucket.inner.remove(b);
                ///DRIVE MECHANIC
                var clone = bucket.clone();
                clone.replace(a, b);
                new_archetypes.add(clone);
            }
        }

        ///For subsets, you only need to check the new archetypes against the opposite archetypes.
        ///This is due to the structure of the way they are created - opposite archetypes won't be self-contained since they were already validated and just may have an attached secondary. The same goes mostly for the like archetypes.
        ///In order to sustain less checks, it is better for new_archetypes to be first due to the order of the double for loop.
        // check_elim_subsets(new_archetypes, opposite_archetypes);
        check_elim_subsets(new_archetypes, inner);

        inner.addAll(new_archetypes);
    }

    /** Checks the subsets and eliminates them from the two lists. */
    public static void check_elim_subsets(ArrayList<Bucket> first, ArrayList<Bucket> second){
        for(var i = 0; i < first.size(); i++){
            var elem_i = first.get(i);
            for(var j = 0; j < second.size(); j++){
                var elem_j = second.get(j);
                if(elem_j.is_superset_of(elem_i)){
                    first.remove(i);
                    i--;
                    break;
                }
                if(elem_i.is_superset_of(elem_j)){
                    second.remove(j);
                    j--;
                }
            }
        }
    }

    /** Display implementation. */
    public String display(){
        var string = new StringBuilder();
        for (Bucket bucket : inner) {
            string.append(bucket.display());
            string.append(" | ");
        }
        if(!string.isEmpty()){
            string.delete(string.length()-3, string.length());
        }
        return string.toString();
    }

    /** Turns the BucketGroup directly into a vec, even removing the HashSet<...> inside Bucket. */
    public ArrayList<ArrayList<Enchantment>> to_vec(){
        
        ArrayList<ArrayList<Enchantment>> output = new ArrayList<>();
        for (Bucket bucket : inner) {
            var out = new ArrayList<Enchantment>();
            for (Enchantment enchantment : bucket.inner) {
                out.add(enchantment);
            }
            output.add(out);
        }

        return output;
    }
}
