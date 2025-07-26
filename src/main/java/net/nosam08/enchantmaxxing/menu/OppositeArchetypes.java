package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;

import net.nosam08.enchantmaxxing.menu.ds.ArchetypesInsert;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;

public class OppositeArchetypes {
    
    /** Uses the opposite archetypes method of creating a menu. */
    public static ArrayList<BucketGroup> opposite_archetypes(ArchetypesInsert insert){
        ArrayList<BucketGroup> built = new ArrayList<>();

        for(var pair : insert.inner.entrySet()){
            var pivot = pair.getKey();
            var rest = pair.getValue();

            //TODO

        }
        



        return null; //TODO
    }

    //merge (puts it in the Vec<BucketGroup>)
    //fuse (puts it in the BucketGroup if needed)








    /** Uses the afterfuse mechanic to simplify/complexify the construction of the Menu. */
    public static MenuInstructions afterfuse(ArrayList<BucketGroup> instructions){
        return null; //TODO
    }

}
