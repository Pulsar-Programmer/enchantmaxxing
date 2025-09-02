package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

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
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.emm.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.emm.component_data.EnchantmentButton;
import net.nosam08.enchantmaxxing.emm.ds.Bucket;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

    ItemStack item;
    ArrayList<BucketGroup> original = new ArrayList<>();

    ArrayList<Integer> selected_bg_idx = new ArrayList<>();

    ///Associated data with the Menu instance.
    protected EnchantmentButton selected_level_button;
    ArrayList<BucketGroupScroller<Component>> horizontal_scrollers = new ArrayList<>();
    // ArrayList<ScrollContainer<Component>> vertical_scroller = new ArrayList<>();
    HashMap<Integer, Pair<Integer, HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>>>> selected_enchantments = new HashMap<>();

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
            client.player.playSound(EnchantifyClient.CONFIG.anvil_apply_sound ? SoundEvents.BLOCK_ANVIL_USE : SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            client.setScreen(null);
            var profile = new EnchantmaxProfile(selected_enchantments);
            Enchantips.start_tooltips(item, profile);
        }).verticalSizing(Sizing.fixed(20));

        var item = Components.item(this.item.copy())
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
        original = EnchantmaxBuilder.to_vec_curses(original, EnchantmaxBuilder.all_enchantments()); //TODO
        for(var i = 0; i < original.size(); i++){
            bucket_groups.add(bucket_group(original.get(i), i));
        }
        return bucket_groups;
    }

    /** Creates the Component and also its proposed size. */
    public Pair<Component, Integer> bucket(Bucket bucket, int b_index, int bg_index){
        ArrayList<Component> children = new ArrayList<>();
        var reg = EnchantmaxBuilder.all_enchantments(); // we should not keep getting the registry TODO
        var levels = EnchantmaxBuilder.levels_map(item);
        bucket.to_vec_curses(reg).forEach(x -> {
            var level = levels.getOrDefault(reg.getId(x), Integer.valueOf(0));
            var button = enchant_level_select(level, reg.getEntry(x), b_index, bg_index);
            children.add(button);
        });

        var vertical = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(children)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        return new Pair<Component, Integer>(vertical, children.size());
    }

    public Component bucket_group(BucketGroup bucketGroup, int bg_index){
        ArrayList<Component> children = new ArrayList<>();
        Integer size = 20;
        for(var i = 0; i < bucketGroup.inner.size(); i++){
            Bucket bucket = bucketGroup.inner.get(i);
            var component = bucket(bucket, i, bg_index);
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

    public ArrayList<Component> generate_levels(int level, RegistryEntry<Enchantment> enchantment){
        var list = new ArrayList<Component>();
        for(var i = level; i <= enchantment.value().getMaxLevel(); i++){
            var text = Text.translatable("enchantment.level." + Integer.toString(i));
            var lvl = Integer.valueOf(i);
            list.add(level_button(text, x -> {
                on_level_select(x, lvl, level, enchantment);
            }));
        }
        return list;
    }

    public static Text enchantment_text(RegistryEntry<Enchantment> e, int level){
        var str = level == 0 ? e.value().description() : Enchantment.getName(e, level);
        var text = e.isIn(EnchantmentTags.CURSE) ? Text.translatable(str.getString()).withColor(0xFA655D) : Text.translatable(str.getString());
        return text;
    }

    public Component enchant_level_select(int level, RegistryEntry<Enchantment> enchantment, int b_index, int bg_index){

        var name = enchantment_text(enchantment, level);
        ArrayList<Component> levels = generate_levels(level, enchantment);

        var horizontal = Containers.horizontalFlow(Sizing.fixed(0), Sizing.content())
        .children(levels)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);
        
        var btn = new EnchantmentButton(name, b -> {
            on_enchant_click(b, horizontal);
        }, b_index, bg_index).margins(Insets.horizontal(3));

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

    // public static Component enchant_button(Text name, Consumer<ButtonComponent> fn){
    //     return Components.button(name, fn).margins(Insets.horizontal(3)).verticalSizing(Sizing.fixed(20));
    // }

    public void on_enchant_click(ButtonComponent b, Component horizontal){
        if(selected_level_button != null){
            selected_level_button.active = true;
            var selected_level_horizontal = selected_level_button.parent().children().get(1);
            selected_level_horizontal.horizontalSizing(Sizing.fixed(0));
        }
        selected_level_button = ((EnchantmentButton)b);
        // selected_level_horizontal = horizontal;
        b.active = false;
        horizontal.horizontalSizing(Sizing.content());
    }

    public void on_level_select(ButtonComponent lvl_btn, int level, int reg_level, RegistryEntry<Enchantment> ench){
        lvl_btn.parent().horizontalSizing(Sizing.fixed(0));
        selected_level_button.active(true);
        
        selected_level_button.setMessage(enchantment_text(ench, level));
        var space = Size.of(selected_level_button.width(), selected_level_button.height());
        var tracer = selected_level_button.parent();
        while(tracer.hasParent()){
            tracer = tracer.parent();
            tracer.layout(space);
        }

        if(level == reg_level){
            unanimate_button(selected_level_button);
        } else {
            ///-> Do not pick up on zero events.
            register_enchantment(ench, level);

            animate_button(selected_level_button);
        }
    }

    /** Register level in output map. */
    public void register_enchantment(RegistryEntry<Enchantment> ench, int level){
        var present = selected_enchantments.getOrDefault(selected_level_button.bg_index, new Pair<>(selected_level_button.b_index, new HashMap<>()));
        if(present.getLeft() == selected_level_button.b_index){
            present.getRight().put(ench, new Pair<Integer,EnchantmentButton>(level, selected_level_button));
            selected_enchantments.put(selected_level_button.bg_index, present);
        } else {
            present.setLeft(selected_level_button.b_index);
            ///Free all the buttons!!
            for (var btn_pair : present.getRight().entrySet()) {
                EnchantmentButton btn = btn_pair.getValue().getRight();
                ///Press the first level button to reset!
                var temp_select_btn = selected_level_button;
                selected_level_button = btn;
                var first_lvl_btn = ((ButtonComponent)((ParentComponent)btn.parent().children().get(1)).children().get(0));
                first_lvl_btn.onPress();
                selected_level_button = temp_select_btn;
            }
            ///Add new selection.
            HashMap<RegistryEntry<Enchantment>, Pair<Integer, EnchantmentButton>> hm = new HashMap<>();
            hm.put(ench, new Pair<Integer,EnchantmentButton>(level, selected_level_button));
            present.setRight(hm);
        }
    }

    /** Makes the button fancy. */
    public void animate_button(EnchantmentButton button){
        client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
        button.enchanted = true;
    }

    /** Makes the button unfancy. */
    public void unanimate_button(EnchantmentButton button){
        button.enchanted = false;
    }






    


    

    





















    








    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
