package net.nosam08.enchantmaxxing.menu.ds;

import java.util.ArrayList;

/**Chart of Compatible Enchantments. */
public class CompatibilityChart<T> {
    ArrayList<TwoSet<T>> inner;

    /** Adds to the Chart without checking for duplicates - unsafe. */
    public void add_unchecked(TwoSet<T> pair){
        pair.sort();
        inner.add(pair);
    }
}
