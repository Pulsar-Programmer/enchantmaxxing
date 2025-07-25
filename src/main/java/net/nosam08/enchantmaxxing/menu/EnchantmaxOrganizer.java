package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;

/** Finds all the possible in-game enchantments and their compatibilites. */
public class EnchantmaxOrganizer {


    public static Iterable<Enchantment> all_enchantments(){
        return MinecraftClient.getInstance()
            .getNetworkHandler()
            .getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);
    }


    
    private static ArchetypesInsert build_from_start(){
        var built = new ArchetypesInsert();

        var all = all_enchantments();
        // var all_acceptor = new ArrayList<Enchantment>();
        for(var ench : all){
            // all_acceptor.add(ench);






            
        }

        var reg_len = all_acceptor.size();

        for(var i = 0; i < reg_len; i++){
            var a = all_acceptor.get(i);

            for(var j = i; j < reg_len; j++){
                var b = all_acceptor.get(j);

                if(a.exc){
                    built.lac_insert(a, b);
                }
            }
        }

        return built;
    }
}
