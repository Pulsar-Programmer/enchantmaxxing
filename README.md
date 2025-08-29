# Final Tinted Touch

FTT helps you fully max out the enchantments on your gear. It is even compatible with mods that add new or modify existing enchantments!

To start, hover over an item and press X to open up a menu to that lets you choose which enchantments should be present in your maxed out items.
The menu features the list of all enchantments in the game, with the bars indicating incompatible paths. (ex. Sharpness | Bane of Arthropods | Smite; Loyalty, Channeling | Riptide)
It will let you choose certain profiles to save and configure.
If you have default profiles on, what is regarded as the best build will be automatically configured - note that this does not consider modded enchants.

Hovering over an item that is configured will list in orange the enchantments that need to be applied to the item to max it out. You can choose which items to show this menu on, including modded ones.

# Config

This mod is compatible with ModMenu.
The complete list of Config material is listed in `src/main/net/nosam08/enchantmaxxing/config/EnchantifyConfig.java`.
It's formatted in `json5` if you want to edit the file in config directly.

# Particularities

To open the maxing menu, you can press X while:
- Holding an Item in your Hand
- Handling an Item in a GUI
- Holding an Item in a GUI

Autofusing is a special mechanism to make the UI of the Enchantment Modification Menu a lot better by fusing options together across different paths.
Toggle UI is a different implementation of the selecting of Enchantments during the Enchantment Modification Menu.

