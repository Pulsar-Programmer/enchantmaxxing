package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

    ArrayList<Component> buttons = new ArrayList<>();

    ArrayList<BucketGroup> original = new ArrayList<>();
    ArrayList<Integer> selected_bg_idx = new ArrayList<>();
    //var output

    public EnchantmaxMenu(ArrayList<BucketGroup> original, ArrayList<Component> buttons){
        this.original = original;
        this.buttons = buttons;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.TOP);

        // buttons = test_buttons(Lists.newArrayList("Sharpness", "Silk Touch", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys"));

        var flow = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(buttons)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.horizontal(3)
        );

        var scroller = Containers.verticalScroll(Sizing.content(), Sizing.fill(), 
            flow
        )
        .scrollbarThiccness(4)
        .scrollbar(ScrollContainer.Scrollbar.vanilla())
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);

        var padder = Containers.verticalFlow(Sizing.content(), Sizing.fill(85))
            .child(scroller)
            .padding(Insets.of(5))
            .surface(Surface.DARK_PANEL)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.vertical(5));

        rootComponent.child(
            padder
        );

        rootComponent.child(
            bottom_menu()
        );

    }

    public Component bottom_menu(){
        var back = Components.button(Text.translatable("option.enchantify.enchantmax.back"), button -> {
            client.setScreen(null);
        }).verticalSizing(Sizing.fixed(20));

        var apply = Components.button(Text.translatable("option.enchantify.enchantmax.apply"), button -> {
            client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            client.setScreen(null);
            Enchantips.start_tooltips();
        }).verticalSizing(Sizing.fixed(20));

        var box = button_box(20, 0);

        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
            .child(back)
            .child(box)
            .child(apply)
            .padding(Insets.of(5))
            .surface(Surface.DARK_PANEL)
            .verticalAlignment(VerticalAlignment.BOTTOM)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.bottom(2));
    }

    public static ArrayList<Component> test_buttons(ArrayList<String> names){
        ArrayList<Component> array = new ArrayList<>();
        names.forEach(x -> {
            array.add(enchant_button(Text.literal(x)));
        });
        return array;
    }

    public static Component enchant_button(Text name){
        return Components.button(name, EnchantmaxMenu::on_enchant_click).margins(Insets.of(0, 6, 3, 3)).verticalSizing(Sizing.fixed(20));
    }

    public static void on_enchant_click(ButtonComponent button){
        //TODO
        //button function
        button.active = !button.active;
        System.out.println("click");
    }

    public static void on_enchant_click_dropdown(DropdownComponent dropdown){
        //TODO
        //button function
        // button.active = !button.active;
        
        System.out.println("click");
    }


    public static Component enchant_dropdown(Text name){
        var dropdown = Components.dropdown(Sizing.content())
            .button(name, EnchantmaxMenu::on_enchant_click_dropdown)
            .button(name, EnchantmaxMenu::on_enchant_click_dropdown)
            .button(name, EnchantmaxMenu::on_enchant_click_dropdown)
            .closeWhenNotHovered(false)
            .margins(Insets.of(0, 6, 3, 3));
        var head = Containers.collapsible(Sizing.content(), Sizing.content(), Text.literal("Collapsible Section"), false)
        .child(dropdown)
        .margins(Insets.of(0, 6, 3, 3));
        return head;
    }



    public static Component button_box(int px_size, int btm_margin){
        var box = new BoxComponent(Sizing.fixed(1), Sizing.fixed(px_size))
        .color(Color.ofRgb(0xDDDDDD)).margins(Insets.of(0, btm_margin, 5, 5));
        return box;
    }



    public static Component bucket(Bucket bucket){
        ArrayList<Component> children = new ArrayList<>();
        var reg = EnchantmaxBuilder.all_enchantments(); // we should not keep getting the registry TODO
        bucket.inner.forEach(x -> {
            var str = Enchantment.getName(reg.getEntry(x), x.getMaxLevel());
            var text = reg.getEntry(x).isIn(EnchantmentTags.CURSE) ? str : Text.translatable(str.getString());
            var button = enchant_dropdown(text);
            children.add(button);
        });
        return Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(children)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);
    }

    public static Component bucket_group(BucketGroup bucketGroup){
        ArrayList<Component> children = new ArrayList<>();
        Integer size = 20;
        for (Bucket bucket : bucketGroup.inner) {
            var component = bucket(bucket);
            size = Math.max(component.height(), size);
            children.add(component);  
        }

        ArrayList<Component> children_lines = new ArrayList<>();
        for (Component component : children) {
            children_lines.add(component);
            children_lines.add(button_box(size, 6)); 
        }
        children_lines.removeLast();

        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
            .children(children_lines)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);
    }











    /** Where all the UI happens based on the direct build. */
    public static Screen direct(ArrayList<BucketGroup> instructions){
        ArrayList<Component> bucket_groups = new ArrayList<>();
        instructions.forEach(x -> bucket_groups.add(bucket_group(x)));

        return new EnchantmaxMenu(instructions, bucket_groups);
    }


    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
