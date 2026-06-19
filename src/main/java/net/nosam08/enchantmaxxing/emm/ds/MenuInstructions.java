package net.nosam08.enchantmaxxing.emm.ds;

import java.util.ArrayList;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.util.Tuple;

public class MenuInstructions {
    public ArrayList<ArrayList<ArrayList<Tuple<Enchantment, Integer>>>> rows;
    //im going vertical

    public MenuInstructions() {
        rows = new ArrayList<>();
    }

    /** Yields a sample display of the instructions. */
    public String sample_display(){
        var string = new StringBuilder();
        for (ArrayList<ArrayList<Tuple<Enchantment,Integer>>> bucket_group : rows) {
            ///PRINT EACH BUCKET GROUP.
            for (ArrayList<Tuple<Enchantment,Integer>> bucket : bucket_group) {
                for (Tuple<Enchantment, Integer> pair : bucket) {
                    string.append(pair.getA().toString());
                    string.append(";");
                    string.append(pair.getB());
                    string.append(" ");
                }
                string.append("\n***\n");
            }
            string.append("\n-----------------\n");
        }
        
        return string.toString();
    }

}
