package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.enchantment.Enchantment;

public class Bucket {
    public HashSet<Enchantment> inner;

    public Bucket(){
        inner = new HashSet<>();
    }

    public Bucket(ArrayList<Enchantment> add){
        this();
        inner.addAll(add);
    }
    
    /** Clones the bucket. */
    public Bucket clone(){
        var cloned = new Bucket();
        cloned.inner.addAll(inner);
        return cloned;
    }

    /** Replaces the element in the Bucket with a different one. */
    public void replace(Enchantment a, Enchantment b){
        inner.remove(a);
        inner.add(b);
    }

    /** Checks if the bucket is a subset of this bucket. */
    public boolean is_superset_of(Bucket subset){
        for (var a : subset.inner) {
            if(!inner.contains(a)){
                return false;
            }
        }
        return true;
    }

    /** Display implementation. */
    public String display(){
        var string = new StringBuilder();
        for (Enchantment enchantment : inner) {
            string.append(enchantment.toString());
            string.append(", ");
        }
        if(!string.isEmpty()){
            string.delete(string.length()-2, string.length());
        }
        return string.toString();
    }

    /** Creates a Name for the Bucket with easier sorting. */
    public String name(){
        ///A typed order preservation is not required since the HashSet itself IS that sorting preservation, albeit in an unclear way.
        var string = new StringBuilder();
        for (Enchantment enchantment : inner) {
            ///I would do ID here, but that would require the exact registry smart pointer, so I'm not doing that here.
            string.append(enchantment.toString());
            string.append(";");
        }
        return string.toString();
    }
}
