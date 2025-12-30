package net.nosam08.enchantmaxxing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.emm.OppositeArchetypes;
import net.nosam08.enchantmaxxing.emm.ds.Bucket;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;

public class Tests {



    //silly java so im printing everything
    public static void printNestedCollections(List<?> list) {
        printNestedCollections(list, 0);
    }

    private static void printNestedCollections(Object value, int depth) {
        if (value instanceof List<?> nestedList) {
            System.out.printf("%s[%s%n", indent(depth), nestedList.getClass().getSimpleName());
            for (Object element : nestedList) {
                printNestedCollections(element, depth + 1);
            }
            System.out.printf("%s]%n", indent(depth));
        } else if (value instanceof Object[]) {
            System.out.printf("%s[%s%n", indent(depth), value.getClass().getComponentType().getSimpleName());
            for (Object element : (Object[]) value) {
                printNestedCollections(element, depth + 1);
            }
            System.out.printf("%s]%n", indent(depth));
        } else {
            System.out.printf("%s- %s%n", indent(depth), value);
        }
    }

    private static String indent(int depth) {
        return "  ".repeat(Math.max(0, depth));
    }






    public static void testing(){
        EnchantifyClient.LOGGER.info("Starting Tests...");
        // test_subsets();
        // test_minifuse();
        // test_fuse();
        // test_merge();
        // test_afterfuse();
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
        var bucket_groups = Lists.newArrayList(bucketgroup("A|BCD|G"));

        var pivot = enchantment("F");
        var rest = Lists.newArrayList(enchantment("G"), enchantment("B"));
        
        var p2 = enchantment("Z");

        var p3 = enchantment("X");
        var r3 = Lists.newArrayList(enchantment("Y"));

        // OppositeArchetypes.merge(bucket_groups, pivot, rest);
        // EnchantifyClient.LOGGER.info(bucket_groups.get(0).display());

        // OppositeArchetypes.merge(bucket_groups, p2, new ArrayList<>());
        // EnchantifyClient.LOGGER.info(bucket_groups.get(1).display());

        // OppositeArchetypes.merge(bucket_groups, p3, r3);
        // EnchantifyClient.LOGGER.info(bucket_groups.get(2).display());
    }

    public static void test_fuse(){
        var bucket_group = bucketgroup("A|BCD|G");
        var pivot = enchantment("F");
        var rest = Arrays.asList(enchantment("G"), enchantment("B"));

        var p3 = enchantment("X");
        var r3 = Arrays.asList(enchantment("Y"));

        OppositeArchetypes.fuse(bucket_group, pivot, new ArrayList<>(rest));
        EnchantifyClient.LOGGER.info(bucket_group.display());

        OppositeArchetypes.fuse(bucket_group, p3, new ArrayList<>(r3));
        EnchantifyClient.LOGGER.info(bucket_group.display());
    }

    public static void test_minifuse(){
        var bucket_group = bucketgroup("A|BC");
        EnchantifyClient.LOGGER.info(bucket_group.display());
        EnchantifyClient.LOGGER.info("1");

        bucket_group.minifuse(enchantment("B"), enchantment("A"));
        EnchantifyClient.LOGGER.info(bucket_group.display());
        EnchantifyClient.LOGGER.info("2");

        bucket_group.minifuse(enchantment("B"), enchantment("D"));
        EnchantifyClient.LOGGER.info(bucket_group.display());
        EnchantifyClient.LOGGER.info("3");

        bucket_group.minifuse(enchantment("B"), enchantment("C"));
        EnchantifyClient.LOGGER.info(bucket_group.display());
        EnchantifyClient.LOGGER.info("4");
    }



    public static void test_subsets(){
        var list_a = new ArrayList<>(Arrays.asList(bucket("ABC"), bucket("BC"), bucket("GH")));
        var list_b = new ArrayList<>(Arrays.asList(bucket("AB"), bucket("BCDGH"), bucket("EF")));
        // an edge case where ABC A / AB AB .. is not possible because A is subset of ABC (subset of subset is subset)

        BucketGroup.check_elim_subsets(list_a, list_b);
        
        list_a.forEach(x -> EnchantifyClient.LOGGER.info(x.display()));
        list_b.forEach(x -> EnchantifyClient.LOGGER.info(x.display()));
    }


    public static void test_afterfuse(){
        var bucket_groups = new ArrayList<BucketGroup>(Arrays.asList(bucketgroup("ABC|BD|GHI")));

        MenuInstructions menu_instructions = OppositeArchetypes.afterfuse(bucket_groups);
        EnchantifyClient.LOGGER.info(menu_instructions.sample_display());

    }


    public static void acceptable_or_primary(){
        var reg = EnchantmaxBuilder.all_enchantments();
        // Sharpness and Smite are mutually incompatible
        var sharpness = reg.getOrThrow(Enchantments.SHARPNESS);
        var smite = reg.getOrThrow(Enchantments.SMITE);

        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.addEnchantment(sharpness, 1);

        var r1 = smite.value().isAcceptableItem(sword);
        EnchantifyClient.LOGGER.info(r1+"");

        var r2 = smite.value().isPrimaryItem(sword);
        EnchantifyClient.LOGGER.info(r2+"");

        var r3 = smite.value().isSupportedItem(sword);
        EnchantifyClient.LOGGER.info(r3+"");
    }

}
