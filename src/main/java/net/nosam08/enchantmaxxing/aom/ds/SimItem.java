package net.nosam08.enchantmaxxing.aom.ds;

public class SimItem {
    public Integer
    pwp;

    

    public SimItem(Integer pwp) {
        this.pwp = pwp;
    }



    /** Clones a SimItem and its fields. */
    public SimItem clone(){
        return new SimItem(Integer.valueOf(pwp));
    }
}
