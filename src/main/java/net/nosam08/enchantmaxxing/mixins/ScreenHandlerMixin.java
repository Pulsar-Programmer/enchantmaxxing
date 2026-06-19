package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"))
    private void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!player.getEntityWorld().isClient()) return;

        ScreenHandler handler = (ScreenHandler)(Object)this;

        if (handler instanceof EnchantmentScreenHandler self) {
            enchantment_click(self, slotIndex);
        } else if (handler instanceof AnvilScreenHandler anvil) {
            anvil_quick_move(anvil, slotIndex, actionType, player);
        }
    }

    /** Shifts the task when the (now enchanted) item leaves the enchanting table slot. */
    private static void enchantment_click(EnchantmentScreenHandler self, int slotIndex) {
        if (slotIndex != 0) return; // slot 0 is the item slot

        Slot slot = self.slots.get(0);
        if (!slot.hasStack()) return;

        ItemStack current = slot.getStack();
        var enchantment_data = Enchantips.effective_enchantments(current); // books store separately
        if (enchantment_data.isEmpty()) return; // no enchantments = nothing to shift

        ItemStack strippedKey = Enchantips.stripEnchantments(current); // = old key (unenchanted)

        var enchantments = new ArrayList<EnchantmentLevelEntry>();
        for (var enchantment : enchantment_data.getEnchantments()) {
            var level = enchantment_data.getLevel(enchantment);
            enchantments.add(new EnchantmentLevelEntry(enchantment, level));
        }

        if (enchantments.isEmpty()) return;

        Enchantips.shift_active_task(strippedKey, enchantments, current);
    }

    /** Handles quick-moving the anvil output. Vanilla empties the output stack before
     * onTakeOutput fires on this path, so the diff must be taken here, pre-mutation. */
    private static void anvil_quick_move(AnvilScreenHandler anvil, int slotIndex, SlotActionType actionType, PlayerEntity player) {
        if (actionType != SlotActionType.QUICK_MOVE) return;
        if (slotIndex != 2) return; // slot 2 is the output slot

        Slot output = anvil.slots.get(2);
        if (!output.hasStack() || !output.canTakeItems(player)) return;

        ItemStack input = anvil.slots.get(AnvilScreenHandler.INPUT_1_ID).getStack();
        if (input.isEmpty()) return;

        ItemStack result = output.getStack();
        var added = Enchantips.added_enchantments(input, result);
        Enchantips.shift_active_task(input.copy(), added, result.copy());
    }
}
