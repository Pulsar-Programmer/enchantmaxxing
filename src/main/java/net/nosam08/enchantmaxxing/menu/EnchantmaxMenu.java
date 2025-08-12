package net.nosam08.enchantmaxxing.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.menu.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.menu.ds.Bucket;
import net.nosam08.enchantmaxxing.menu.ds.BucketGroup;
import net.nosam08.enchantmaxxing.menu.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

    ItemStack item;
    ArrayList<BucketGroup> original = new ArrayList<>();

    ArrayList<Integer> selected_bg_idx = new ArrayList<>();

    ///Associated data with the Menu instance.
    ButtonComponent selected_level_button;
    Component selected_level_horizontal;
    ArrayList<BucketGroupScroller<Component>> horizontal_scrollers = new ArrayList<>();
    
    // ArrayList<ScrollContainer<Component>> vertical_scroller = new ArrayList<>();
    //var output
    // ArrayList<EnchantmentLevelEntry> selected_enchantments; //output?
    // HashMap<String, Integer> selected_enchantments; > potentially stronger?

    public EnchantmaxMenu(ItemStack item, ArrayList<BucketGroup> original){
        this.item = item;
        this.original = original;
    }

    /** Where all the UI happens based on the direct build. */
    public static Screen direct(ItemStack item, ArrayList<BucketGroup> instructions){
        return new EnchantmaxMenu(item, instructions);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (Screen.hasShiftDown()) {
            horizontal_scrollers.forEach(x -> {
                x.onMouseScroll(mouseX, mouseY, horizontalAmount);
            });
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /** Handles updates for the Screen. */
    @Override
    public void tick() {
        super.tick();

        horizontal_scrollers.forEach(x->x.tick(width));
        // vertical_scroller.forEach(x -> {
        //     var size = Math.min(x.child().fullSize().height() + 5, (int)(0.85 * height));
        //     x.verticalSizing(Sizing.fixed(size));
        // });
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

        var buttons = buttons();

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

        // vertical_scroller.add(scroller);

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

    /** Builds the bottom menu specifying the options. */
    public Component bottom_menu(){
        var back = Components.button(Text.translatable("option.enchantify.enchantmax.back"), button -> {
            client.setScreen(null);
        }).verticalSizing(Sizing.fixed(20));

        var apply = Components.button(Text.translatable("option.enchantify.enchantmax.apply"), button -> {
            client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            client.setScreen(null);
            // Enchantips.start_tooltips(); TODO
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

    /** Creates the tiny box separating the buckets. */
    public static Component button_box(int px_size){
        var box = new BoxComponent(Sizing.fixed(1), Sizing.fixed(px_size))
        .color(Color.ofRgb(0xDDDDDD)).margins(Insets.horizontal(5));
        return box;
    }

    /** Creates the buttons for the main part of the menu. */
    public ArrayList<Component> buttons(){
        ArrayList<Component> bucket_groups = new ArrayList<>();
        original.forEach(x -> bucket_groups.add(bucket_group(x)));
        return bucket_groups;
    }

    /** Creates the Component and also its proposed size. */
    public Pair<Component, Integer> bucket(Bucket bucket){
        ArrayList<Component> children = new ArrayList<>();
        var reg = EnchantmaxBuilder.all_enchantments(); // we should not keep getting the registry TODO
        var levels = EnchantmaxBuilder.levels_map(item);
        bucket.inner.forEach(x -> {
            var level = levels.getOrDefault(reg.getId(x), Integer.valueOf(0));
            var text = enchantment_text(reg.getEntry(x), level);
            var button = enchant_level_select(text, level, x.getMaxLevel());
            children.add(button);
        });

        var vertical = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(children)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        return new Pair<Component, Integer>(vertical, children.size());
    }

    public Component bucket_group(BucketGroup bucketGroup){
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

        var h_scroll = BucketGroupScroller.bucket_group_scroller(container);

        horizontal_scrollers.add(h_scroll);

        return h_scroll
        .scrollbarThiccness(0)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(5));
    }

    public ArrayList<Component> generate_levels(int level, int max_level){
        var list = new ArrayList<Component>();
        for(var i = level; i <= max_level; i++){
            var text = i == 0 ? Text.literal("Ø") : Text.translatable("enchantment.level." + Integer.toString(i));
            var integer = Integer.valueOf(i);
            list.add(level_button(text, x -> {
                var lvl = integer;
                on_level_select(x);
                //TODO
            }));
        }
        return list;
    }

    public static Text enchantment_text(RegistryEntry<Enchantment> e, int level){
        var str = level == 0 ? e.value().description() : Enchantment.getName(e, level);
        var text = e.isIn(EnchantmentTags.CURSE) ? Text.translatable(str.getString()).withColor(0xFA655D) : Text.translatable(str.getString());
        return text;
    }

    public Component enchant_level_select(Text name, int level, int max_level){

        ArrayList<Component> levels = generate_levels(level, max_level);

        var horizontal = Containers.horizontalFlow(Sizing.fixed(0), Sizing.content())
        .children(levels)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);
        
        var btn = Components.button(name, b -> {
            if(selected_level_button != null){
                selected_level_button.active = true;
                selected_level_horizontal.horizontalSizing(Sizing.fixed(0));
            }
            selected_level_button = b;
            selected_level_horizontal = horizontal;
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

    

    public static Component level_button(Text name, Consumer<ButtonComponent> fn){
        return Components.button(name, fn).verticalSizing(Sizing.fixed(20));
    }

    public static Component enchant_button(Text name, Consumer<ButtonComponent> fn){
        return Components.button(name, fn).margins(Insets.horizontal(3)).verticalSizing(Sizing.fixed(20));
    }




    public static void basic_click(ButtonComponent button){
        System.out.println("Click!");
    }

    public void on_enchant_click(ButtonComponent button){
        //TODO
        //button function
        button.active = !button.active;
        System.out.println("click");
    }

    public void on_level_select(ButtonComponent button){
        //register level
        //close button menu
        button.parent().horizontalSizing(Sizing.fixed(0));
        // button.parent().children().get(0).active(true);
        selected_level_button.active(true);
        //make button fancy
        button.parent().parent().padding(Insets.of(2)).surface(Surface.PANEL_INSET.and(Surface.outline(0x1AD4FF) )); //Surface.outline(0x1AD4FF)

        // selected_level_button
        
        System.out.println("click");
    }










    


    

    





















    








    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
