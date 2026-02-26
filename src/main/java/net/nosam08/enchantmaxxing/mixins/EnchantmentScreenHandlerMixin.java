package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
    @Inject(method = "updateSlotStacks", at = @At("HEAD"))
    private void onSlotsUpdated(int revision, List<ItemStack> stacks, ItemStack cursorStack, CallbackInfo ci) {
        // if (!player.getWorld().isClient()) return;
        // stacks.get(0) is the item slot, now with enchantments applied by server
        ItemStack result = stacks.get(0);
        if (!result.hasEnchantments()) return; // nothing enchanted
        
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
