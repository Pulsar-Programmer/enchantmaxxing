package net.nosam08.enchantmaxxing;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.menu.OppositeArchetypes;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

public class Tests {

    public static void testing(){
        // Enchantify.LOGGER.info("Starting Tests...");
        // test_subsets();
        // test_minifuse();
        // test_fuse();
        // test_merge();
        test_afterfuse();
    }

    public static Enchantment enchantment(String name){
        return new Enchantment(Text.of(name), null, null, null);
    }

    public static Bucket bucket(String contents){
        var a = new Bucket();
        contents.chars().forEach(c -> a.inner.add(enchantment(Character.toString(c))));
        return a;
    }

    public static BucketGroup bucketgroup(String contents){
        var a = new BucketGroup();
        var str = new StringBuilder();
        contents.chars().forEach(c -> {
            if(c == '|'){
                a.inner.add(bucket(str.toString()));
                str.delete(0, str.length());
            } else{
                str.append(Character.toString(c));
            }
        });
        a.inner.add(bucket(str.toString()));
        return a;
    }

    public static void test_merge(){
        var bucket_groups = new ArrayList<BucketGroup>(Arrays.asList(bucketgroup("A|BCD|G")));

        var pivot = enchantment("F");
        var rest = Arrays.asList(enchantment("G"), enchantment("B"));
        
        var p2 = enchantment("Z");

        var p3 = enchantment("X");
        var r3 = Arrays.asList(enchantment("Y"));

        OppositeArchetypes.merge(bucket_groups, pivot, new ArrayList<>(rest));
        Enchantify.LOGGER.info(bucket_groups.get(0).display());

        OppositeArchetypes.merge(bucket_groups, p2, new ArrayList<>());
        Enchantify.LOGGER.info(bucket_groups.get(1).display());

        OppositeArchetypes.merge(bucket_groups, p3, new ArrayList<>(r3));
        Enchantify.LOGGER.info(bucket_groups.get(2).display());
    }

    public static void test_fuse(){
        var bucket_group = bucketgroup("A|BCD|G");
        var pivot = enchantment("F");
        var rest = Arrays.asList(enchantment("G"), enchantment("B"));

        var p3 = enchantment("X");
        var r3 = Arrays.asList(enchantment("Y"));

        OppositeArchetypes.fuse(bucket_group, pivot, new ArrayList<>(rest));
        Enchantify.LOGGER.info(bucket_group.display());

        OppositeArchetypes.fuse(bucket_group, p3, new ArrayList<>(r3));
        Enchantify.LOGGER.info(bucket_group.display());
    }

    public static void test_minifuse(){
        var bucket_group = bucketgroup("A|BC");
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("1");

        bucket_group.minifuse(enchantment("B"), enchantment("A"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("2");

        bucket_group.minifuse(enchantment("B"), enchantment("D"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("3");

        bucket_group.minifuse(enchantment("B"), enchantment("C"));
        Enchantify.LOGGER.info(bucket_group.display());
        Enchantify.LOGGER.info("4");
    }



    public static void test_subsets(){
        var list_a = new ArrayList<>(Arrays.asList(bucket("ABC"), bucket("BC"), bucket("GH")));
        var list_b = new ArrayList<>(Arrays.asList(bucket("AB"), bucket("BCDGH"), bucket("EF")));
        // an edge case where ABC A / AB AB .. is not possible because A is subset of ABC (subset of subset is subset)

        BucketGroup.check_elim_subsets(list_a, list_b);
        
        list_a.forEach(x -> Enchantify.LOGGER.info(x.display()));
        list_b.forEach(x -> Enchantify.LOGGER.info(x.display()));
    }


    public static void test_afterfuse(){
        var bucket_groups = new ArrayList<BucketGroup>(Arrays.asList(bucketgroup("ABC|BD|GHI")));

        MenuInstructions menu_instructions = OppositeArchetypes.afterfuse(bucket_groups);
        Enchantify.LOGGER.info(menu_instructions.sample_display());

    }

}
