package net.nosam08.enchantmaxxing;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;

public class Tests {

    public static void testing(){
        test_subsets();
    }

    public static Enchantment test_enchantment(String name){
        return new Enchantment(Text.of(name), null, null, null);
    }

    public static Bucket test_bucket(String contents){
        var a = new Bucket();
        contents.chars().forEach(c -> a.inner.add(test_enchantment(Character.toString(c))));
        return a;
    }
    //TODO this is like the edge case - fn still works 
    public static void test_subsets(){
        var list_a = new ArrayList<>(Arrays.asList(test_bucket("ABC"), test_bucket("A")));
        var list_b = new ArrayList<>(Arrays.asList(test_bucket("AB"), test_bucket("AB"), test_bucket("AB")));

        BucketGroup.check_elim_subsets(list_a, list_b);
        
        list_a.forEach(x -> Enchantify.LOGGER.info(x.display()));
        list_b.forEach(x -> Enchantify.LOGGER.info(x.display()));
    }

}
