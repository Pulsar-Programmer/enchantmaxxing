package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {


    @Unique
    private ItemStack captured_old = null;

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    private void captureInput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient()) return;
        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;
        // Capture a copy before the method consumes the slot
        captured_old = self.slots.get(AnvilScreenHandler.INPUT_1_ID).getStack().copy();
    }

    @Inject(method = "onTakeOutput", at = @At("RETURN"))
    private void onAnvil(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient()) return;
        if (captured_old == null || captured_old.isEmpty()) return;

        ///we wanna take the difference from before and after enchantments
        var before_data = captured_old.getEnchantments();
        var after_data = stack.getEnchantments();
        ArrayList<EnchantmentLevelEntry> added_enchantments = new ArrayList<>();

        ///cmp and find enchantments
        for (var new_ench : after_data.getEnchantments()) {
            int previous_level = before_data.getLevel(new_ench);
            int now_level = after_data.getLevel(new_ench);
            if(now_level > previous_level){
                added_enchantments.add(new EnchantmentLevelEntry(new_ench, now_level));
            }
        }
        Enchantips.shift_active_task(captured_old, added_enchantments, stack);
        captured_old = null;
    }
}
