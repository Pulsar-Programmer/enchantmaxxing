package net.nosam08.enchantmaxxing.config;

public enum CurseOrderOptions {
    TOP("option.enchantify.top", 0x98C379),
    BOTTOM("option.enchantify.bottom", 0xE5C07B),
    OFF("option.enchantify.off", 0xFB655D), //FB655D < maybe try w/o? idk
    RANDOM("option.enchantify.random", 0xFFFFFF);
    
    private final String display;
    private final int color;
    
    public int getColor() {
        return color;
    }

    CurseOrderOptions(String display, int color) {
        this.display = display;
        this.color = color;
    }
    
    @Override
    public String toString() {
        return display;
    }
}
