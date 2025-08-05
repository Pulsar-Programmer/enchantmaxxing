package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

/** Builds the list of enchantments that will be displayed on the EnchantmaxMenu */
public class EnchantmaxBuilder {

    /** Builds the core and direct menu instructions given the item to Enchantmax. */
    public static ArrayList<BucketGroup> build_direct(ItemStack item){
        var enchantments = all_enchantments();
        Stream<Enchantment> stream = StreamSupport.stream(enchantments.spliterator(), false).filter((Enchantment x) -> x.isSupportedItem(item));

        stream = stream.filter(ench_i -> ench_i.isAcceptableItem(item));
        //actually - this is fine for dynamic/static whatever because isAcceptableItem only checks item not enchantment
        // item.canBeEnchantedWith() >> this is PERFECT!!!

        var insert = build_from_start(stream);
        
        var instructions = OppositeArchetypes.opposite_archetypes(insert);
        return instructions;
    }
    
    /** Builds the menu appearance given the item to Enchantmax. */
    public static MenuInstructions build(ItemStack item){
        var instructions = build_direct(item);
        return OppositeArchetypes.afterfuse(instructions);
    }


    

    /** Returns all enchantments. */
    public static Iterable<Enchantment> all_enchantments(){
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
