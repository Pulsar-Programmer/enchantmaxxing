package net.nosam08.enchantmaxxing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

/**
 * Drops the active enchant task when an item is ground down. We hook the output slot's
 * {@code onTake} so the task is only removed once the player actually takes the de-enchanted
 * result, not when they merely place or pull back an input. {@code GrindstoneMenu$4} is the
 * output slot (the only inner slot that overrides {@code onTake}).
 */
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class GrindstoneScreenHandlerMixin {

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onGrindOutputTaken(Player player, ItemStack stack, CallbackInfo ci) {
        // ACTIVE_TASKS is client-side state; only act there.
        if (!player.level().isClientSide()) return;

        AbstractContainerMenu handler = player.containerMenu;
        if (!(handler instanceof GrindstoneMenu grindstone)) return;

        // Inputs are still present at HEAD (onTake consumes them). Each input that carried
        // enchantments had its own task keyed off it; grinding clears those enchantments, so drop
        // the corresponding tasks.
        clearTaskFor(grindstone, GrindstoneMenu.INPUT_SLOT);
        clearTaskFor(grindstone, GrindstoneMenu.ADDITIONAL_SLOT);
    }

    private void clearTaskFor(GrindstoneMenu grindstone, int slotIndex) {
        ItemStack input = grindstone.getSlot(slotIndex).getItem();
        if (input.isEmpty() || !input.isEnchanted()) return;
        Enchantips.grindstone_task(input);
    }
}
