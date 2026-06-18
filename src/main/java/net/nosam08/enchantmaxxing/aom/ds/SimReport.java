package net.nosam08.enchantmaxxing.aom.ds;

import net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering;

public class SimReport {
    public Integer //repr(Cost)
    pwp1,
    pwp2,
    sacrifice;
    public String operation;

    public SimReport(Integer pwp1, Integer pwp2, Integer sacrifice, String operation) {
        this.pwp1 = pwp1;
        this.pwp2 = pwp2;
        this.sacrifice = sacrifice;
        this.operation = operation;
    }



    public static SimReport combine(SimItem o, SimEnchantment x){
        var new_ident = "(" + o.identifier + "," + x.identifier + ")";
        var rpr = new SimReport(Integer.valueOf(o.pwp), Integer.valueOf(x.pwp), Integer.valueOf(x.cost), new_ident);
        o.pwp = AnvilOrdering.adv(o.pwp);
        o.identifier = new_ident;
        return rpr;
    }
}
