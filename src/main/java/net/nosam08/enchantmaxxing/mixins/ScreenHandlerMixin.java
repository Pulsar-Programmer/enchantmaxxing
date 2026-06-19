package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {

    // 26.1: AbstractContainerMenu#clicked(int, int, ContainerInput, Player) — was Yarn onSlotClick/SlotActionType.
    @Inject(method = "clicked", at = @At("HEAD"))
    private void onSlotClick(int slotIndex, int button, ContainerInput actionType, Player player, CallbackInfo ci) {
        if (!player.level().isClientSide()) return;

        AbstractContainerMenu handler = (AbstractContainerMenu)(Object)this;

        if (handler instanceof EnchantmentMenu self) {
            enchantment_click(self, slotIndex);
        } else if (handler instanceof AnvilMenu anvil) {
            anvil_quick_move(anvil, slotIndex, actionType, player);
        }
    }

    /** Shifts the task when the (now enchanted) item leaves the enchanting table slot. */
    private static void enchantment_click(EnchantmentMenu self, int slotIndex) {
        if (slotIndex != 0) return; // slot 0 is the item slot

        Slot slot = self.slots.get(0);
        if (!slot.hasItem()) return;

        ItemStack current = slot.getItem();
        var enchantment_data = Enchantips.effective_enchantments(current); // books store separately
        if (enchantment_data.isEmpty()) return; // no enchantments = nothing to shift

        ItemStack strippedKey = Enchantips.stripEnchantments(current); // = old key (unenchanted)

        var enchantments = new ArrayList<EnchantmentInstance>();
        for (var enchantment : enchantment_data.keySet()) {
            var level = enchantment_data.getLevel(enchantment);
            enchantments.add(new EnchantmentInstance(enchantment, level));
        }

        if (enchantments.isEmpty()) return;

        Enchantips.shift_active_task(strippedKey, enchantments, current);
    }

    /** Handles quick-moving the anvil output. Vanilla empties the output stack before
     * onTake fires on this path, so the diff must be taken here, pre-mutation. */
    private static void anvil_quick_move(AnvilMenu anvil, int slotIndex, ContainerInput actionType, Player player) {
        if (actionType != ContainerInput.QUICK_MOVE) return;
        if (slotIndex != 2) return; // slot 2 is the output slot

        Slot output = anvil.slots.get(2);
        if (!output.hasItem() || !output.mayPickup(player)) return;

        ItemStack input = anvil.slots.get(AnvilMenu.INPUT_SLOT).getItem();
        if (input.isEmpty()) return;

        ItemStack result = output.getItem();
        var added = Enchantips.added_enchantments(input, result);
        Enchantips.shift_active_task(input.copy(), added, result.copy());
    }
}
