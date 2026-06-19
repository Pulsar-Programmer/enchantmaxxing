package net.nosam08.enchantmaxxing.config;

public class EnchantifyConfig {
    /**The color of enchantments that appear under the item. */
    public Integer hoverColor = 0xFFA500;
    
    /** Skips the `X` button press required to start maxing out an item. */
    //automatically applies the default profile of the item
    public boolean defaultX = true;

    /** Determines whether to enchantmax enchantments that are already applied. */
    public boolean is_static = false;

    /**
     * Forces every enchantment to be treated as combinable, ignoring exclusive sets entirely.
     * Escape hatch for compatibility mods that unlock combining (e.g. stacking all protections)
     * by overriding the anvil's combine logic *without* going through
     * {@link net.minecraft.world.item.enchantment.Enchantment#areCompatible} — those can't be auto-detected,
     * so this lets the user opt in manually. Applied live the next time a menu is opened.
     */
    public boolean force_combinable = false;





    /** Determines whether to use the Autofuse menu or not. */
    public boolean do_afterfuse = false; //should be turned on in later versions of the Autofuse Update

    /** Determines whether to use the Anvil sound when clicking apply to your item. */
    public boolean anvil_apply_sound = true;

    /** Determines whether to implement the fancy menu or the regular one. */
    public boolean do_fancy_menu = false; //TODO

    /** Determines the order of the curses in the menu. */
    public CurseOrderOptions curse_order = CurseOrderOptions.BOTTOM;



    // /** Determines whether to do anvil prioritization notes. */
    // public boolean do_anvil_notes = false;

    // /** The color of anvil prioritization notes. */
    // public Integer next_fuse_color = 0x00A8FF;
}