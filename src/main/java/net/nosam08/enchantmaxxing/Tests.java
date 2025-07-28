package net.nosam08.enchantmaxxing;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;

public class Tests {

    public static void testing(){
        Enchantify.LOGGER.info("Starting Tests...");
        // test_subsets();
        test_minifuse();
    }

    public static Enchantment test_enchantment(String name){
        return new Enchantment(Text.of(name), null, null, null);
    }

    public static Bucket test_bucket(String contents){
        var a = new Bucket();
        contents.chars().forEach(c -> a.inner.add(test_enchantment(Character.toString(c))));
        return a;
    }

    //TODO error with this
    public static BucketGroup test_bucketgroup(String contents){
        var a = new BucketGroup();
        var str = new StringBuilder();
        contents.chars().forEach(c -> {
            if(c == '|'){
                a.inner.add(test_bucket(str.toString()));
                Enchantify.LOGGER.info(str.toString());
                str.delete(0, str.length());
            } else{
                str.append(Character.toString(c));
            }
        });
        a.inner.add(test_bucket(str.toString()));
        return a;
    }

    public static void test_merge(){
        //TODO
    }

    public static void test_fuse(){
        //TODO
    }

    public static void test_minifuse(){
        var bucket_group = test_bucketgroup("A|BC");
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("1");

        bucket_group.minifuse(test_enchantment("B"), test_enchantment("A"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("2");

        bucket_group.minifuse(test_enchantment("B"), test_enchantment("D"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("3");

        //stuck in infinite loop
        bucket_group.minifuse(test_enchantment("B"), test_enchantment("C"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("4");
    }



    public static void test_subsets(){
        var list_a = new ArrayList<>(Arrays.asList(test_bucket("ABC"), test_bucket("BC"), test_bucket("GH")));
        var list_b = new ArrayList<>(Arrays.asList(test_bucket("AB"), test_bucket("BCDGH"), test_bucket("EF")));
        // an edge case where ABC A / AB AB .. is not possible because A is subset of ABC (subset of subset is subset)

        BucketGroup.check_elim_subsets(list_a, list_b);
        
        list_a.forEach(x -> Enchantify.LOGGER.info(x.display()));
        list_b.forEach(x -> Enchantify.LOGGER.info(x.display()));
    }

}
