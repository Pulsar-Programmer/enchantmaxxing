package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
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
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

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

        var buttons = test_buttons(Lists.newArrayList("Sharpness", "Silk Touch", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys", "Potato Monkeys"));

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

        //TODO - apply function
        var apply = Components.button(Text.translatable("option.enchantify.enchantmax.apply"), button -> {
            System.out.println("apply");
        }).verticalSizing(Sizing.fixed(20));

        var box = button_box();

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

    public ArrayList<Component> test_buttons(ArrayList<String> names){
        ArrayList<Component> array = new ArrayList<>();
        names.forEach(x -> {
            array.add(enchant_button(x));
        });
        return array;
    }

    public Component enchant_button(String name){
        return Components.button(Text.literal(name), button -> {
            //button function
            // button.active = false;
            button.active = !button.active;
            System.out.println("click");
        }).margins(Insets.of(0, 6, 3, 3)).verticalSizing(Sizing.fixed(20));
    }

    public Component button_box(){
        var box = new BoxComponent(Sizing.fixed(1), Sizing.fixed(16))
        .color(Color.ofRgb(0xDDDDDD)).margins(Insets.horizontal(5));
        return box;
    }















    /** Where all the UI happens based on the direct build. */
    public static Screen direct(ArrayList<BucketGroup> instructions){
        
        return null; //TODO
    }


    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){

        return null; //TODO
    }
}
