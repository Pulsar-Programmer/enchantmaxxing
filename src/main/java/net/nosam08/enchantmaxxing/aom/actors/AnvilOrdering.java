package net.nosam08.enchantmaxxing.aom.actors;

import java.util.Optional;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;
// import net.nosam08.enchantmaxxing.aom.ds.SimEnchantment;
// import net.nosam08.enchantmaxxing.aom.ds.SimItem;
// import net.nosam08.enchantmaxxing.aom.ds.SimReport;
// import net.nosam08.enchantmaxxing.aom.ds.SimReportTree;
import net.nosam08.enchantmaxxing.emm.EnchantmaxBuilder;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

public class AnvilOrdering {
    



    public static OrderString ordering(ItemStackKey item, EnchantmaxProfile enchantments){
        //TODO INSERT THE TRUSTY TRUE ITEM HERE
        return new OrderString(deserialize_enchantment("sharpness:3"), "(OBJ,((minecraft:book,minecraft:sharpness;5),minecraft:feather))");
    } //minecraft:sharpness;1



    // public static void evaluate(){
    //     //TODO
    // }






    // public static SimReportTree one_set(SimItem o, SimEnchantment x){
    //     var operation = SimReport.combine(o, x);

    //     return new SimReportTree(operation);
    // }

    // public static SimReportTree n_set(SimItem o, SimEnchantment operation){
        
    // }


    /** Advances the PWP of an object. */
    public static Integer adv(Integer x){
        return x * 2 + 1;
    }




    /** Creates a String from the enchantment. */
    public static String serialize_enchantment(EnchantmentLevelEntry entry){
        return entry.enchantment.getIdAsString() + ";" + entry.level;
    }

    /** Creates an ItemStack from the Enchantment. */
    public static ItemStack deserialize_enchantment(String enchantment) {
        String[] parts = enchantment.split(";");
        if (parts.length != 2) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        String enchantmentId = parts[0];
        int level = Integer.parseInt(parts[1]);
        
        Identifier id = Identifier.tryParse(enchantmentId);
        if (id == null) {
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
        
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        
        RegistryWrapper<Enchantment> enchantmentRegistry = EnchantmaxBuilder.all_enchantments();
        Optional<RegistryEntry.Reference<Enchantment>> enchantmentEntry = enchantmentRegistry.getOptional(
            RegistryKey.of(RegistryKeys.ENCHANTMENT, id)
        );

        book.addEnchantment(enchantmentEntry.get(), level);
        
        return book;
    }



}
