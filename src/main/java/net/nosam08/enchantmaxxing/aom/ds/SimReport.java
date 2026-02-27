package net.nosam08.enchantmaxxing.aom.ds;

import net.minecraft.util.Pair;
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
        var rpr = new SimReport(Integer.valueOf(o.pwp), Integer.valueOf(x.pwp), Integer.valueOf(x.cost), "(OBJ," + x.identifier + ")");
        o.pwp = AnvilOrdering.adv(o.pwp);
        return rpr;
    }

    public static Pair<SimEnchantment, SimReport> merged(SimEnchantment x, SimEnchantment y) {
        var merged_enchantment = SimEnchantment.merged(x, y);
        var rpr = new SimReport(Integer.valueOf(x.pwp), Integer.valueOf(y.pwp), Integer.valueOf(y.cost), new String(merged_enchantment.identifier));
        return new Pair<SimEnchantment,SimReport>(merged_enchantment, rpr);
    }

    public Integer exp_sum(){
        return pwp1 + pwp2 + sacrifice;
    }
}
