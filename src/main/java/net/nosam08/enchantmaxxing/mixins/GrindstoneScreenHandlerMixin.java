package net.nosam08.enchantmaxxing.mixins;

import java.util.ArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

@Mixin(GrindstoneScreenHandler.class)
public class GrindstoneScreenHandlerMixin {

    @Unique
    private ItemStack captured_og = null;

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void onContentChanged(Inventory inventory, CallbackInfo ci) {
        GrindstoneScreenHandler self = (GrindstoneScreenHandler)(Object)this;
        
        ItemStack slot0 = self.slots.get(0).getStack();
        ItemStack slot1 = self.slots.get(1).getStack();
        // ItemStack output = self.slots.get(2).getStack();

        // Only capture when there's a valid output ready to be taken
        // if (output.isEmpty()) return;

        if (slot0.hasEnchantments()) {
            captured_og = slot0.copy();
        } else if (slot1.hasEnchantments()) {
            captured_og = slot1.copy();
        }

        // new ItemStackKey(captured_og).read(); //here
    }

    @Inject(method = "onContentChanged", at = @At("RETURN"))
    private void onItemTaken(Inventory inventory, CallbackInfo ci) {
        if (captured_og == null) return;

        GrindstoneScreenHandler self = (GrindstoneScreenHandler)(Object)this;
        ItemStack slot0 = self.slots.get(0).getStack();
        ItemStack slot1 = self.slots.get(1).getStack();
        ItemStack output = self.slots.get(2).getStack();

        if(slot0 == null){
            System.out.println("Slot 0 Null!");
        } else {
            new ItemStackKey(slot0).read();
        }
        if(slot1 == null){
            System.out.println("Slot 1 Null!");
        } else {
            new ItemStackKey(slot1).read();
        }
        if(output == null){
            System.out.println("Output Null!");
        } else {
            new ItemStackKey(output).read();
        }
        

        // // Inputs cleared + no output = player took the result
        // if (!slot0.isEmpty() || !slot1.isEmpty() || !output.isEmpty()) return;

        var enchantments = new ArrayList<EnchantmentLevelEntry>();
        var enchantment_data = captured_og.getEnchantments();
        for (var enchantment : enchantment_data.getEnchantments()) {
            enchantments.add(new EnchantmentLevelEntry(enchantment, enchantment_data.getLevel(enchantment)));
        }

        Enchantips.grindstone_task(captured_og, enchantments, grind(captured_og));
        captured_og = null;
    }

    private ItemStack grind(ItemStack item) {
        ItemEnchantmentsComponent itemEnchantmentsComponent = EnchantmentHelper.apply(item, (components) -> components.remove((enchantment) -> !enchantment.isIn(EnchantmentTags.CURSE)));
        if (item.isOf(Items.ENCHANTED_BOOK) && itemEnchantmentsComponent.isEmpty()) {
            item = item.withItem(Items.BOOK);
        }
        return item;
    }
}
