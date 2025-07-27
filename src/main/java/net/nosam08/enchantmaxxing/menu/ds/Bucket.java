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
}
