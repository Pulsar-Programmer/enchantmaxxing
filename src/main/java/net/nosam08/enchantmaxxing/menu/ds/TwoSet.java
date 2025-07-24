package net.nosam08.enchantmaxxing.menu.ds;

import java.util.Arrays;

/** Rust {@code [T; 2]} Port to Java */
public class TwoSet<T> {
    T one;
    T two;

    public void sort(){
        var one = this.one.toString();
        var two = this.two.toString();

        var mut = new String[]{one, two};
        Arrays.sort(mut);
        one = mut[0];
        two = mut[1];
    }
}
