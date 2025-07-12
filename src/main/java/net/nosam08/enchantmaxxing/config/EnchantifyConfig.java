package net.nosam08.enchantmaxxing.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "enchantify")
public class EnchantifyConfig implements ConfigData {
    public boolean toggleA = true;
    public boolean toggleB = false;
    
    @ConfigEntry.Gui.CollapsibleObject
    InnerStuff stuff = new InnerStuff();
    
    @ConfigEntry.Gui.Excluded
    InnerStuff invisibleStuff = new InnerStuff();
    
    static class InnerStuff {
        int a = 0;
        int b = 1;
    }
}