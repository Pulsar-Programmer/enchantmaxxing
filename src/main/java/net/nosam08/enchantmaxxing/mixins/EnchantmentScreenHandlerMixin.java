package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
    @Inject(method = "onButtonClick", at = @At("RETURN"))
    private void onEnchant(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // enchant didn't happen

        EnchantmentScreenHandler self = (EnchantmentScreenHandler)(Object)this;
        ItemStack result = self.slots.get(0).getStack();

        ///we know the input is always the plain item so we can take advantage of that here
        ItemStack item = Enchantips.stripEnchantments(result);
        var enchantments = new ArrayList<EnchantmentLevelEntry>();
        var enchantment_data = result.getEnchantments();
        for (var enchantment : enchantment_data.getEnchantments()) {
            var level = enchantment_data.getLevel(enchantment);
            enchantments.add(new EnchantmentLevelEntry(enchantment, level));
        }

        Enchantips.shift_active_task(item, enchantments, result);
    }
}
