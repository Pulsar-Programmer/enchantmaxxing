# Final Tinted Touch

FTT helps you fully max out the enchantments on your gear. It is even compatible with mods that add new or modify existing enchantments!

To start, hover over an item and press X to open up a menu to that lets you choose which enchantments should be present in your maxed out items.
The menu features the list of all enchantments in the game, with the bars indicating incompatible paths. (ex. Sharpness | Bane of Arthropods | Smite; Loyalty, Channeling | Riptide)
It will let you choose certain profiles to save and configure.
If you have default profiles on, what is regarded as the best build will be automatically configured - note that this does not consider modded enchants.

Hovering over an item that is configured will list in orange the enchantments that need to be applied to the item to max it out. You can choose which items to show this menu on, including modded ones.

# Profiles

A Profile is a remembered loadout; the exact set of enchantments and levels you want, saved once and ready to pour onto any item you press X over.

Open the menu and look to the bottom right. That little bar is the whole system: `[ + ✔ | None ]`. Click anywhere on it and a dropdown climbs upward, listing **None** at the top, then **Default**, then every Profile you have made glowing green below.

The green ones are yours. Press `+` to make a new one; it grabs whatever you have selected on screen right then and lets you name it. Press the blue `✔` to save over the active Profile after you tweak it. Press the red `✕` to delete one you are done with.

**None** and **Default** are White Profiles. They are read-only; you cannot rename them, save over them, or delete them.

**Default** is the best build (community standard) there is for that item, maxed all the way out and already curated so nothing fights anything else. Every item carries its own Default; a sword's is not a pickaxe's. It ships inside the mod, so it reads the same on every world you join. Modded items usually have none, and that is fine; they simply fall back to None.

Flip on `defaultX` and the menu steps out of the way completely. Press X over a piece of gear and its Default lands instantly. If there is no Default to find, the regular menu opens like always.

You build the loadout once. The gear remembers.

# Config

This mod is compatible with ModMenu.
The complete list of Config material is listed in `src/main/net/nosam08/enchantmaxxing/config/EnchantifyConfig.java`.
It's formatted in `json5` if you want to edit the file in config directly.

Everything this mod remembers lives under `config`, in plain readable files you can open, back up, or hand to a friend:

- `config/enchantify.json5` is the config itself; every option from `EnchantifyConfig.java`.
- `config/ftt/profiles/{profile_name}.json` is one file per green Profile you make. The name you type becomes the file name.
- `config/ftt/sp/<world>/profiles.json` and `config/ftt/mp/<server>/profiles.json` hold the live maxxing jobs you have started, kept per world and per server so a singleplayer save and a multiplayer server never step on each other.
- The White **Default** Profiles ride along inside the mod at `assets/enchantmaxxing/defaults.json`.

# Particularities

To open the maxing menu, you can press X while:
- Holding an Item in your Hand
- Handling an Item in a GUI
- Holding an Item in a GUI

Autofusing is a special mechanism to make the UI of the Enchantment Modification Menu a lot better by fusing options together across different paths.
Toggle UI is a different implementation of the selecting of Enchantments during the Enchantment Modification Menu.

You can press Y to open a menu of current tasks that are assigned. You can click the circle next to a task to see the graph of which order it should combine in, or you can also see it inlay. You can also assign a button to open the enchantmax graph of something naturally.

# Version Nomenclature

The nomenclature follows (MAJOR).(MINOR).(MINISCULE)

In a standardized UPDATE model to be specified with CHANGELOG, it is presented as the following:

| Type      | Repo Pattern | Description                    |
| --------- | ------------ | ------------------------------ |
| Major     | Milestone    | Large Update                   |
| Minor     | Issue        | New Feature                    |
| Miniscule | Commit       | Bug Fix, Dependency Bump, etc. |

Releases typically follow the (MILESTONE).(ISSUE).(COMMIT) pattern. Version 1.0.0 is essentially an MVP.

# Dependency Guide

The following table represents a guide for the dependencies used in what versions.

| FTT               | Fabric Loader | Cloth Config | Owo Lib        | Mod Menu (optional) |
| ----------------- | ------------- | ------------ | -------------- | ------------------- |
| **2.0.0+1.21.4**  | 0.19.3        | 17.0.144     | 0.12.20+1.21.4 | 13.0.3              |
