package net.nosam08.enchantmaxxing.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import me.shedaniel.math.Color;

@Config(name = "enchantify")
public class EnchantifyConfig implements ConfigData {

    @Comment("The color of enchantments that appear under the item.")
    public Color hoverColor = Color.ofRGB(0xFF, 0xA5, 0x00);
    
    @Comment("Skips the `X` button press required to start maxing out an item.")
    public boolean defaultX = true;
    
}