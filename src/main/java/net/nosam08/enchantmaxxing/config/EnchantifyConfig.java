package net.nosam08.enchantmaxxing.config;

public class EnchantifyConfig {
    /**The color of enchantments that appear under the item. */
    public Integer hoverColor = 0xFFA500;
    
    /** Skips the `X` button press required to start maxing out an item. */
    public boolean defaultX = true;

    /** Determines whether to enchantmax enchantments that are already applied. */
    public boolean is_static = false;





    /** Determines whether to use the Autofuse menu or not. */
    public boolean do_afterfuse = false; //should be turned on in later versions of the Autofuse Update

    /** Determines whether to use the Anvil sound when clicking apply to your item. */
    public boolean anvil_apply_sound = true;

    /** Determines whether to implement the fancy menu or the regular one. */
    public boolean do_fancy_menu = false;
}