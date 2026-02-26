package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
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
    @Inject(method = "onTakeOutput", at = @At("RETURN"))
    private void onAnvil(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!player.getWorld().isClient()) return;

        AnvilScreenHandler self = (AnvilScreenHandler)(Object)this;
        ItemStack input = self.slots.get(AnvilScreenHandler.INPUT_1_ID).getStack(); // left input slot

        ///we wanna take the difference from before and after enchantments
        var before_data = input.getEnchantments();
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
        Enchantips.shift_active_task(input, added_enchantments, stack);
    }
}
