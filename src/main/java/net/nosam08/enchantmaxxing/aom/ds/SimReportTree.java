package net.nosam08.enchantmaxxing.aom.ds;

import java.util.ArrayList;

public class SimReportTree {
    SimReport current;
    ArrayList<SimReportTree> disciples;

    public SimReportTree(SimReport current){
        this.current = current;
        disciples = new ArrayList<>();
    }

    public void open(SimReportTree disciple){
        disciples.add(disciple);
    }
}
