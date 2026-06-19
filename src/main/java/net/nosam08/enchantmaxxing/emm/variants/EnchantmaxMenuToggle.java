package net.nosam08.enchantmaxxing.emm.variants;

import java.util.ArrayList;

import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.emm.EnchantmaxMenu;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;

public class EnchantmaxMenuToggle extends EnchantmaxMenu {

    public EnchantmaxMenuToggle(ItemStack item, ArrayList<BucketGroup> original) {
        super(item, original);
    }

    public ArrayList<UIComponent> generate_levels(int level, RegistryEntry<Enchantment> enchantment){
        var list = new ArrayList<UIComponent>();
        for(var i = level + 1; i <= enchantment.value().getMaxLevel(); i++){
            var text = Text.translatable("enchantment.level." + Integer.toString(i));
            var lvl = Integer.valueOf(i);
            list.add(level_button(text, x -> {
                on_level_select(x, lvl, level, enchantment);
            }));
        }
        return list;
    }

    // public void on_enchant_click(ButtonComponent b, UIComponent horizontal){
    //     if(selected_level_button != null){
    //         selected_level_button.active = true;
    //         var selected_level_horizontal = selected_level_button.parent().children().get(1);
    //         selected_level_horizontal.horizontalSizing(Sizing.fixed(0));
    //     }
    //     selected_level_button = ((EnchantmentButton)b);
    //     // selected_level_horizontal = horizontal;
    //     b.active = false;
    //     horizontal.horizontalSizing(Sizing.content());
    // }

    // public void on_level_select(ButtonComponent lvl_btn, int level, int reg_level, RegistryEntry<Enchantment> ench){
    //     lvl_btn.parent().horizontalSizing(Sizing.fixed(0));
    //     selected_level_button.active(true);
        
    //     selected_level_button.setMessage(enchantment_text(ench, level));
    //     var space = Size.of(selected_level_button.width(), selected_level_button.height());
    //     var tracer = selected_level_button.parent();
    //     while(tracer.hasParent()){
    //         tracer = tracer.parent();
    //         tracer.layout(space);
    //     }

    //     if(level == reg_level){
    //         unanimate_button(selected_level_button);
    //     } else {
    //         ///-> Do not pick up on zero events.
    //         register_enchantment(ench, level);

    //         animate_button(selected_level_button);
    //     }
    // }
    
}
