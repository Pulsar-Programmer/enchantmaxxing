package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient()) return;
        
        if (!(((ScreenHandler)(Object)this) instanceof EnchantmentScreenHandler self)) return;
        if (slotIndex != 0) return; // slot 0 is the item slot
        
        Slot slot = self.slots.get(0);
        if (!slot.hasStack()) return;
        
        ItemStack current = slot.getStack();
        if (!current.hasEnchantments()) return; // no enchantments = nothing to shift
        
        ItemStack strippedKey = Enchantips.stripEnchantments(current); // = old key (unenchanted)
        
        var enchantments = new ArrayList<EnchantmentLevelEntry>();
        var enchantment_data = current.getEnchantments();
        for (var enchantment : enchantment_data.getEnchantments()) {
            var level = enchantment_data.getLevel(enchantment);
            enchantments.add(new EnchantmentLevelEntry(enchantment, level));
        }
        
        if (enchantments.isEmpty()) return;
        
        Enchantips.shift_active_task(strippedKey, enchantments, current);
    }
}