package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.nosam08.enchantmaxxing.Enchantify;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {

    /** Builds the core and direct menu instructions given the item to Enchantmax. */
    public static ArrayList<BucketGroup> build_direct(ItemStack item){

        Registry<Enchantment> enchantments;
        try {
            enchantments = all_enchantments();
        } catch (Exception e) {
            return new ArrayList<>();
        }

        Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter(ench_i -> ench_i.isSupportedItem(item));

        if(!Enchantify.CONFIG.is_static){
            stream = stream.filter(ench_i -> is_compatible(item, ench_i));
        }

        var insert = build_from_start(stream);

        // System.out.println(insert.display());
        
        var instructions = OppositeArchetypes.opposite_archetypes(insert);
        return instructions;
    }

    /** Checks whether an enchantment, "in an anvil", can be applied to the item. */
    public static boolean is_compatible(ItemStack item, Enchantment enchantment){
        var reg = all_enchantments();

        for (var ench_x : item.getEnchantments().getEnchantments()) {
            var ench_x_val = ench_x.value();
            var id_ench_x = reg.getId(ench_x_val);
            if(id_ench_x.equals(reg.getId(enchantment))){
                return false;
            }

            for (var ench_y : ench_x_val.exclusiveSet()) {
                var ench_y_val = ench_y.value();
                var id_ench_y = reg.getId(ench_y_val);
                if(id_ench_y.equals(reg.getId(enchantment))){
                        return false;
                }
            }
        }

        return true;
    }
    
    /** Builds the menu appearance given the item to Enchantmax. */
    public static MenuInstructions build_afterfuse(ItemStack item){
        var instructions = build_direct(item);
        return OppositeArchetypes.afterfuse(instructions);
    }


    

    /** Returns all enchantments. */
    public static Registry<Enchantment> all_enchantments(){
        return MinecraftClient.getInstance()
            .getNetworkHandler()
            .getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);
    }

    /** Builds the ArchetypesInsert from a stream. */
    public static ArchetypesInsert build_from_start(Stream<Enchantment> all){
        var built = new ArchetypesInsert();

        all.forEach(ench -> {
            ench.exclusiveSet().forEach(x -> built.oa_insert(ench, x.value()));
        });

        return built;
    }
}
