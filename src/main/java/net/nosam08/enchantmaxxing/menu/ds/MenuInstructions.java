package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.Pair;

public class MenuInstructions {
    public ArrayList<ArrayList<ArrayList<Pair<Enchantment, Integer>>>> rows;

    public MenuInstructions() {
        rows = new ArrayList<>();
    }

    /** Creates {@code n} zeroes as requested. */
    public static ArrayList<Integer> n_zeroes(int n){
        var build = new ArrayList<Integer>();
        for(var _i = 0; _i < n; _i++) build.add(0);
        return build;
    }

}
