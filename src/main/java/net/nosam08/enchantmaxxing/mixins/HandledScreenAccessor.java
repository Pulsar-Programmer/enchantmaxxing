package net.nosam08.enchantmaxxing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

@Mixin(AbstractContainerScreen.class)
public interface HandledScreenAccessor {
    // 26.1 (Mojang names): the hovered slot field is `hoveredSlot` (was Yarn `focusedSlot`).
    @Accessor("hoveredSlot")
    Slot getFocusedSlot();
}
