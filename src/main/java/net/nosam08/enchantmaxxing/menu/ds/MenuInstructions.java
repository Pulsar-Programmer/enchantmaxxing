package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;

public class MenuInstructions {
    public ArrayList<ArrayList<ArrayList<Pair<Enchantment, Integer>>>> rows;
    //im going vertical

    public MenuInstructions() {
        rows = new ArrayList<>();
    }

    /** Yields a sample display of the instructions. */
    public String sample_display(){
        var string = new StringBuilder();
        for (ArrayList<ArrayList<Pair<Enchantment,Integer>>> bucket_group : rows) {
            ///PRINT EACH BUCKET GROUP.
            for (ArrayList<Pair<Enchantment,Integer>> bucket : bucket_group) {
                for (Pair<Enchantment, Integer> pair : bucket) {
                    string.append(pair.getLeft().toString());
                    string.append(";");
                    string.append(pair.getRight());
                    string.append(" ");
                }
                string.append("\n***\n");
            }
            string.append("\n-----------------\n");
        }
        
        return string.toString();
    }

}
