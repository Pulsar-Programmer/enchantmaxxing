package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.enchantment.Enchantment;

public class Bucket {
    public HashSet<Enchantment> inner;

    public Bucket(){
        inner = new HashSet<>();
        //TODO maybe add some kind of Sorting Preservation after adding an element?
    }

    public Bucket(ArrayList<Enchantment> inner){
        this();
        inner.addAll(inner);
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
        string.delete(string.length()-2, string.length());
        return string.toString();
    }
}
