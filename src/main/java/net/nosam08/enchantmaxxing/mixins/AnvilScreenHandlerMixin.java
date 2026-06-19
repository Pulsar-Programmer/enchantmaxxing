package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(AnvilMenu.class)
public class AnvilScreenHandlerMixin {


    @Unique
    private ItemStack captured_old = null;

    // 26.1: AnvilMenu#onTake(Player, ItemStack) — was Yarn onTakeOutput.
    @Inject(method = "onTake", at = @At("HEAD"))
    private void captureInput(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide()) return;
        AnvilMenu self = (AnvilMenu)(Object)this;
        // Capture a copy before the method consumes the slot
        captured_old = self.slots.get(AnvilMenu.INPUT_SLOT).getItem().copy();
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void onAnvil(Player player, ItemStack stack, CallbackInfo ci) {
        if (!player.level().isClientSide()) return;
        if (captured_old == null || captured_old.isEmpty()) return;
        if (stack.isEmpty()) { captured_old = null; return; } //quick-move path: handled in ScreenHandlerMixin pre-mutation

        ///we wanna take the difference from before and after enchantments
        ArrayList<EnchantmentInstance> added_enchantments = Enchantips.added_enchantments(captured_old, stack);
        Enchantips.shift_active_task(captured_old, added_enchantments, stack);
        captured_old = null;
    }
}
