package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.function.Consumer;

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
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.menu.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

    ArrayList<Component> buttons = new ArrayList<>();

    ArrayList<BucketGroup> original = new ArrayList<>();
    ItemStack item;
    ArrayList<Integer> selected_bg_idx = new ArrayList<>();

    static ButtonComponent level_selection; //not sure how to fix it and make it not static
    static Component level_horizontal; //not sure how to fix it and make it not static
    static ArrayList<BucketGroupScroller<Component>> scrollers = new ArrayList<>();
    //var output

    public EnchantmaxMenu(ItemStack item, ArrayList<BucketGroup> original, ArrayList<Component> buttons){
        this.buttons = buttons;
        this.item = item;
        this.original = original;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Screen.hasShiftDown()) {
            scrollers.forEach(x -> {
                x.onMouseScroll(mouseX, mouseY, horizontalAmount);
            });
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

    /** Builds the bottom menu. */
    public Component bottom_menu(){
        var back = Components.button(Text.translatable("option.enchantify.enchantmax.back"), button -> {
            client.setScreen(null);
        }).verticalSizing(Sizing.fixed(20));

        var apply = Components.button(Text.translatable("option.enchantify.enchantmax.apply"), button -> {
            client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            client.setScreen(null);
            Enchantips.start_tooltips();
        }).verticalSizing(Sizing.fixed(20));

        var item = Components.item(this.item)
        .margins(Insets.both(10, 2));

        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
            .child(back)
            // .child(button_box(20))
            .child(item)
            // .child(button_box(20))
            .child(apply)
            .padding(Insets.of(5))
            .surface(Surface.DARK_PANEL)
            .verticalAlignment(VerticalAlignment.BOTTOM)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.bottom(2));
    }

    public static Component button_box(int px_size){
        var box = new BoxComponent(Sizing.fixed(1), Sizing.fixed(px_size))
        .color(Color.ofRgb(0xDDDDDD)).margins(Insets.horizontal(5));
        return box;
    }



    




    /** Handles updates for the Screen. */
    @Override
    public void tick() {
        super.tick();

        scrollers.forEach(x->x.tick(width));
    }














    



    public static Component enchant_button(Text name, Consumer<ButtonComponent> fn){
        return Components.button(name, fn).margins(Insets.horizontal(3)).verticalSizing(Sizing.fixed(20));
    }

    public static Component level_button(Text name, Consumer<ButtonComponent> fn){
        return Components.button(name, fn).verticalSizing(Sizing.fixed(20));
    }

    public static Component enchant_level_select(Text name){

        ///TODO generate these
        ArrayList<Component> levels = Lists.newArrayList(level_button(Text.literal("I"), EnchantmaxMenu::basic_click), level_button(Text.literal("II"), EnchantmaxMenu::basic_click), level_button(Text.literal("III"), EnchantmaxMenu::basic_click));

        var horizontal = Containers.horizontalFlow(Sizing.fixed(0), Sizing.content())
        .children(levels)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);
        
        var btn = Components.button(name, b -> {
            if(level_selection != null){
                level_selection.active = true;
                level_horizontal.horizontalSizing(Sizing.fixed(0));
                // level_selection.horizontal 
            }
            level_selection = b;
            level_horizontal = horizontal;
            b.active = false;
            horizontal.horizontalSizing(Sizing.content());
        }).margins(Insets.horizontal(3));

        var head = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .child(btn)
        .child(horizontal)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);

        return head;
    }




    public static void basic_click(ButtonComponent button){
        System.out.println("Click!");
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










    


    /** Creates the Component and also its proposed size. */
    public static Pair<Component, Integer> bucket(Bucket bucket){
        ArrayList<Component> children = new ArrayList<>();
        var reg = EnchantmaxBuilder.all_enchantments(); // we should not keep getting the registry TODO
        bucket.inner.forEach(x -> {
            var str = Enchantment.getName(reg.getEntry(x), x.getMaxLevel());
            var text = reg.getEntry(x).isIn(EnchantmentTags.CURSE) ? str : Text.translatable(str.getString());
            var button = enchant_level_select(text);
            children.add(button);
        });

        var vertical = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(children)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        return new Pair<Component, Integer>(vertical, children.size());
    }


    public static Component bucket_group(BucketGroup bucketGroup){
        ArrayList<Component> children = new ArrayList<>();
        Integer size = 20;
        for (Bucket bucket : bucketGroup.inner) {
            var component = bucket(bucket);
            size = Math.max(component.getRight() * 26, size); //does the removed 6 of margin ruin it?
            children.add(component.getLeft());  
        }

        ArrayList<Component> children_lines = new ArrayList<>();
        for (Component component : children) {
            children_lines.add(component);
            children_lines.add(button_box(size));
        }
        children_lines.removeLast();

        var container = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .children(children_lines)
        // .padding(Insets.both(0, 2))
        // .surface(Surface.PANEL_INSET) //WELLS effect 
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(6));

        // container.baseX()
        // Sizing

        // var scroller_size = Math.min(85, container.fullSize());
        var h_scroll = BucketGroupScroller.bucket_group_scroller(container);

        scrollers.add(h_scroll);

        return h_scroll
        .scrollbarThiccness(0)
        // .padding(Insets.of(2))
        // .surface(Surface.PANEL_INSET)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(5));
    }

    





















    /** Where all the UI happens based on the direct build. */
    public static Screen direct(ItemStack item, ArrayList<BucketGroup> instructions){
        ArrayList<Component> bucket_groups = new ArrayList<>();
        instructions.forEach(x -> bucket_groups.add(bucket_group(x)));

        return new EnchantmaxMenu(item, instructions, bucket_groups);
    }








    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
