package net.nosam08.enchantmaxxing.aom.ds;

public class SimItem {
    public Integer
    pwp;
    String identifier;

    

    public SimItem(Integer pwp, String identifier) {
        this.pwp = pwp;
        this.identifier = identifier;
    }



    /** Clones a SimItem and its fields. */
    public SimItem clone(){
        return new SimItem(Integer.valueOf(pwp), new String(identifier));
    }
}
