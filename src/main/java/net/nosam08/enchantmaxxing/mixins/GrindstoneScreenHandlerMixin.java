package net.nosam08.enchantmaxxing.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

/**
 * Drops the active enchant task when an item is ground down. We hook the output slot's
 * {@code onTakeItem} — the grindstone's equivalent of the anvil's {@code onTakeOutput} — so the
 * task is only removed once the player actually takes the de-enchanted result, not when they
 * merely place or pull back an input. {@code GrindstoneScreenHandler$4} is the output slot
 * (the only inner slot that overrides {@code onTakeItem}).
 */
@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerMixin {

    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onGrindOutputTaken(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        // ACTIVE_TASKS is client-side state; only act there.
        if (!player.getWorld().isClient()) return;

        ScreenHandler handler = player.currentScreenHandler;
        if (!(handler instanceof GrindstoneScreenHandler grindstone)) return;

        // Inputs are still present at HEAD (onTakeItem consumes them). Each input that carried
        // enchantments had its own task keyed off it; grinding clears those enchantments, so drop
        // the corresponding tasks.
        clearTaskFor(grindstone, GrindstoneScreenHandler.INPUT_1_ID);
        clearTaskFor(grindstone, GrindstoneScreenHandler.INPUT_2_ID);
    }

    private void clearTaskFor(GrindstoneScreenHandler grindstone, int slotIndex) {
        ItemStack input = grindstone.getSlot(slotIndex).getStack();
        if (input.isEmpty() || !input.hasEnchantments()) return;
        Enchantips.grindstone_task(input);
    }
}
