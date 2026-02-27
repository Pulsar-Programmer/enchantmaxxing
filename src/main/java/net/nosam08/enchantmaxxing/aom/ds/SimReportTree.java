package net.nosam08.enchantmaxxing.aom.ds;

import java.util.ArrayList;

public class SimReportTree {
    public SimReport current;
    public ArrayList<SimReportTree> disciples;

    public SimReportTree(SimReport current){
        this.current = current;
        disciples = new ArrayList<>();
    }

    public void open(SimReportTree disciple){
        disciples.add(disciple);
    }
}
