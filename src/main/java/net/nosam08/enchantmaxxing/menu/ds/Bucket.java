package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.config.CurseOrderOptions;
import net.nosam08.enchantmaxxing.menu.EnchantmaxBuilder;

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

    /** Transforms the Bucket into an iterable vec that considers the ordering of curses.*/
    public ArrayList<Enchantment> to_vec_curses(Registry<Enchantment> reg){
        var first = EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.TOP) ? 1 : 0;
        var second = EnchantifyClient.CONFIG.curse_order.equals(CurseOrderOptions.BOTTOM) ? 2 : 0;
        var num = first + second;

        var main = new ArrayList<Enchantment>();
        if(num == 0){
            main.addAll(inner);
            return main;
        }

        var curses = new ArrayList<Enchantment>();
        for (Enchantment enchantment : inner) {
            var entry = reg.getEntry(enchantment);
            if(entry.isIn(EnchantmentTags.CURSE)){
                curses.add(enchantment);
            } else {
                main.add(enchantment);
            }
        }

        if(num == 2){
            main.addAll(curses);
            return main;
        } else {
            curses.addAll(main);
            return curses;
        }
    }
}
