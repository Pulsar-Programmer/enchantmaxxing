package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.emm.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.emm.component_data.EnchantmentButton;
import net.nosam08.enchantmaxxing.emm.ds.Bucket;
import net.nosam08.enchantmaxxing.emm.ds.BucketGroup;
import net.nosam08.enchantmaxxing.emm.ds.MenuInstructions;
import net.nosam08.enchantmaxxing.profiles.DefaultProfiles;
import net.nosam08.enchantmaxxing.profiles.Profiles;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;

public class EnchantmaxMenu extends BaseOwoScreen<FlowLayout> {

    ItemStack item;
    ArrayList<BucketGroup> original = new ArrayList<>();

    ArrayList<Integer> selected_bg_idx = new ArrayList<>();

    ///Associated data with the Menu instance.
    protected EnchantmentButton selected_level_button;
    ArrayList<BucketGroupScroller<UIComponent>> horizontal_scrollers = new ArrayList<>();
    // ArrayList<ScrollContainer<UIComponent>> vertical_scroller = new ArrayList<>();
    HashMap<Integer, Tuple<Integer, HashMap<Holder<Enchantment>, Tuple<Integer, EnchantmentButton>>>> selected_enchantments = new HashMap<>();

    /// --- Profiles ---
    /** The active *user* (green) profile, or {@code null} for the white "None"/"Default" profiles. */
    private String active_profile = null;
    /** True when the read-only white "Default" profile is the active one. */
    private boolean default_active = false;
    /** Bottom-right container holding the optional dropdown/naming row above the [+ | name] bar. */
    private FlowLayout profile_area;
    /** Shows white "None" or the green active profile name. */
    private LabelComponent profile_label;
    /** The open profile dropdown, or {@code null} when closed. */
    private FlowLayout profile_dropdown;
    /** The open name-entry row, or {@code null} when not naming. */
    private FlowLayout naming_row;
    /** Every selectable enchantment on this item, keyed by id, so a profile can re-select it. */
    private final HashMap<String, EnchantSlot> enchant_index = new HashMap<>();

    /** The pieces needed to programmatically select an enchantment at a chosen level. */
    private static class EnchantSlot {
        final EnchantmentButton button;
        final int base_level;
        final ParentUIComponent level_row;
        EnchantSlot(EnchantmentButton button, int base_level, ParentUIComponent level_row) {
            this.button = button;
            this.base_level = base_level;
            this.level_row = level_row;
        }
    }

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
        if (EnchantifyClient.hasShiftDown()) {
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
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.TOP);

        var buttons = buttons();

        var flow = UIContainers.verticalFlow(Sizing.content(), Sizing.content())
            .children(buttons)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.horizontal(3)
        );

        var scroller = UIContainers.verticalScroll(Sizing.content(), Sizing.fill(), 
            flow
        )
        .scrollbarThiccness(4)
        .scrollbar(ScrollContainer.Scrollbar.vanilla())
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);

        // vertical_scroller.add(scroller);

        var padder = UIContainers.verticalFlow(Sizing.content(), Sizing.fill(85))
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

        // Floats over the bottom-right corner; its dropdown grows upward out of the bar.
        rootComponent.child(profile_selector());

    }

    /** Builds the bottom menu specifying the options. */
    public UIComponent bottom_menu(){
        var back = UIComponents.button(Component.translatable("option.enchantify.enchantmax.back"), button -> {
            minecraft.setScreen(null);
        }).verticalSizing(Sizing.fixed(20));

        var apply = UIComponents.button(Component.translatable("option.enchantify.enchantmax.apply"), button -> {
            minecraft.player.playSound(EnchantifyClient.CONFIG.anvil_apply_sound ? SoundEvents.ANVIL_USE : SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            minecraft.setScreen(null);
            var profile = new EnchantmaxProfile(selected_enchantments);
            Enchantips.start_tooltips(item, profile);
        }).verticalSizing(Sizing.fixed(20));

        var item = UIComponents.item(this.item.copy())
        .margins(Insets.both(10, 2));

        return UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
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
    public static UIComponent button_box(int px_size){
        var box = new BoxComponent(Sizing.fixed(1), Sizing.fixed(px_size))
        .color(Color.ofRgb(0xDDDDDD)).margins(Insets.horizontal(5));
        return box;
    }

    /** Creates the buttons for the main part of the menu. */
    public ArrayList<UIComponent> buttons(){
        ArrayList<UIComponent> bucket_groups = new ArrayList<>();
        original = EnchantmaxBuilder.to_vec_curses(original, EnchantmaxBuilder.all_enchantments()); //TODO
        for(var i = 0; i < original.size(); i++){
            bucket_groups.add(bucket_group(original.get(i), i));
        }
        return bucket_groups;
    }

    /** Creates the UIComponent and also its proposed size. */
    public Tuple<UIComponent, Integer> bucket(Bucket bucket, int b_index, int bg_index){
        ArrayList<UIComponent> children = new ArrayList<>();
        var reg = EnchantmaxBuilder.all_enchantments(); // we should not keep getting the registry TODO
        var levels = EnchantmaxBuilder.levels_map(item);
        bucket.to_vec_curses(reg).forEach(x -> {
            var level = levels.getOrDefault(reg.getKey(x), Integer.valueOf(0));
            var button = enchant_level_select(level, reg.wrapAsHolder(x), b_index, bg_index);
            children.add(button);
        });

        var vertical = UIContainers.verticalFlow(Sizing.content(), Sizing.content())
            .children(children)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER);

        return new Tuple<UIComponent, Integer>(vertical, children.size());
    }

    public UIComponent bucket_group(BucketGroup bucketGroup, int bg_index){
        ArrayList<UIComponent> children = new ArrayList<>();
        Integer size = 20;
        for(var i = 0; i < bucketGroup.inner.size(); i++){
            Bucket bucket = bucketGroup.inner.get(i);
            var component = bucket(bucket, i, bg_index);
            size = Math.max(component.getB() * 26, size); //does the removed 6 of margin ruin it?
            children.add(component.getA());
        }

        ArrayList<UIComponent> children_lines = new ArrayList<>();
        for (UIComponent component : children) {
            children_lines.add(component);
            children_lines.add(button_box(size));
        }
        children_lines.removeLast();

        var container = UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
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

    public ArrayList<UIComponent> generate_levels(int level, Holder<Enchantment> enchantment){
        var list = new ArrayList<UIComponent>();
        for(var i = level; i <= enchantment.value().getMaxLevel(); i++){
            var text = Component.translatable("enchantment.level." + Integer.toString(i));
            var lvl = Integer.valueOf(i);
            list.add(level_button(text, x -> {
                on_level_select(x, lvl, level, enchantment);
            }));
        }
        return list;
    }

    public static Component enchantment_text(Holder<Enchantment> e, int level){
        var str = level == 0 ? e.value().description() : Enchantment.getFullname(e, level);
        var text = e.is(EnchantmentTags.CURSE) ? Component.translatable(str.getString()).withColor(0xFA655D) : Component.translatable(str.getString());
        return text;
    }

    public UIComponent enchant_level_select(int level, Holder<Enchantment> enchantment, int b_index, int bg_index){

        var name = enchantment_text(enchantment, level);
        ArrayList<UIComponent> levels = generate_levels(level, enchantment);

        var horizontal = UIContainers.horizontalFlow(Sizing.fixed(0), Sizing.content())
        .children(levels)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);
        
        EnchantmentButton btn = new EnchantmentButton(name, b -> {
            on_enchant_click(b, horizontal);
        }, b_index, bg_index);
        btn.margins(Insets.horizontal(3));

        var head = UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
        .child(btn)
        .child(horizontal)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);

        // Index this enchantment so a loaded profile can re-select it without a real mouse click.
        enchant_index.put(enchantment.getRegisteredName(), new EnchantSlot(btn, level, horizontal));

        return head;
    }

    

    public static UIComponent level_button(Component name, Consumer<ButtonComponent> fn){
        return UIComponents.button(name, fn).verticalSizing(Sizing.fixed(20));
    }

    // public static UIComponent enchant_button(Component name, Consumer<ButtonComponent> fn){
    //     return UIComponents.button(name, fn).margins(Insets.horizontal(3)).verticalSizing(Sizing.fixed(20));
    // }

    public void on_enchant_click(ButtonComponent b, UIComponent horizontal){
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

    public void on_level_select(ButtonComponent lvl_btn, int level, int reg_level, Holder<Enchantment> ench){
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
            unregister_enchantment(ench, level);

            unanimate_button(selected_level_button);
        } else {
            ///-> Do not pick up on zero events.
            register_enchantment(ench, level);

            animate_button(selected_level_button);
        }
    }

    /** Register level in output map. */
    public void register_enchantment(Holder<Enchantment> ench, int level){
        var present = selected_enchantments.getOrDefault(selected_level_button.bg_index, new Tuple<>(selected_level_button.b_index, new HashMap<>()));
        if(present.getA() == selected_level_button.b_index){
            present.getB().put(ench, new Tuple<Integer,EnchantmentButton>(level, selected_level_button));
            selected_enchantments.put(selected_level_button.bg_index, present);
        } else {
            present.setA(selected_level_button.b_index);
            ///Free all the buttons!!
            for (var btn_pair : present.getB().entrySet()) {
                EnchantmentButton btn = btn_pair.getValue().getB();
                ///Press the first level button to reset!
                var temp_select_btn = selected_level_button;
                selected_level_button = btn;
                var first_lvl_btn = ((ButtonComponent)((ParentUIComponent)btn.parent().children().get(1)).children().get(0));
                // 1.21.9: ButtonWidget#onPress needs the triggering input; owo only forwards to its
                // press consumer, so a synthetic left-click stands in for a programmatic press.
                first_lvl_btn.onPress(new MouseButtonInfo(0, 0));
                selected_level_button = temp_select_btn;
            }
            ///Add new selection.
            HashMap<Holder<Enchantment>, Tuple<Integer, EnchantmentButton>> hm = new HashMap<>();
            hm.put(ench, new Tuple<Integer,EnchantmentButton>(level, selected_level_button));
            present.setB(hm);
        }
    }

    /** Unregister level in output map. */
    public void unregister_enchantment(Holder<Enchantment> ench, int level){
        var present = selected_enchantments.getOrDefault(selected_level_button.bg_index, new Tuple<>(selected_level_button.b_index, new HashMap<>()));
        present.getB().remove(ench);
    }

    /** Makes the button fancy. */
    public void animate_button(EnchantmentButton button){
        minecraft.player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
        button.enchanted = true;
    }

    /** Makes the button unfancy. */
    public void unanimate_button(EnchantmentButton button){
        button.enchanted = false;
    }

    /// ----------------------------------------------------------------------
    /// Profiles: reusable, world-independent enchant-selection presets.
    /// ----------------------------------------------------------------------

    /** Builds the bottom-right profile control: a [+ | name] bar that grows a dropdown upward. */
    public UIComponent profile_selector(){
        profile_label = UIComponents.label(Component.literal("None")).color(Color.ofArgb(0xFFFFFFFF));
        profile_label.shadow(true);
        profile_label.cursorStyle(CursorStyle.HAND);
        profile_label.margins(Insets.horizontal(6));
        profile_label.tooltip(Component.literal("Select Profile"));
        profile_label.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                toggle_dropdown();
                return true;
            }
            return false;
        });

        var plus = UIComponents.label(Component.literal("+")).color(Color.ofArgb(0xFF40FF40));
        plus.shadow(true);
        plus.cursorStyle(CursorStyle.HAND);
        plus.margins(Insets.of(0, 0, 4, 4));
        plus.tooltip(Component.literal("Add Profile"));
        plus.mouseEnter().subscribe(() -> plus.color(Color.ofArgb(0xFF80FF80)));
        plus.mouseLeave().subscribe(() -> plus.color(Color.ofArgb(0xFF40FF40)));
        plus.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                start_naming();
                return true;
            }
            return false;
        });

        // Blue checkmark: overwrite the active profile with the current on-screen selection.
        var save = UIComponents.label(Component.literal("✔")).color(Color.ofArgb(0xFF55AAFF));
        save.shadow(true);
        save.cursorStyle(CursorStyle.HAND);
        save.margins(Insets.of(0, 0, 2, 4));
        save.tooltip(Component.literal("Save Profile"));
        save.mouseEnter().subscribe(() -> save.color(Color.ofArgb(0xFF80C8FF)));
        save.mouseLeave().subscribe(() -> save.color(Color.ofArgb(0xFF55AAFF)));
        save.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                overwrite_active_profile();
                return true;
            }
            return false;
        });

        var bar = UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
            .child(plus)
            .child(save)
            .child(button_box(16))
            .child(profile_label);
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.horizontalAlignment(HorizontalAlignment.CENTER);
        bar.padding(Insets.of(4, 4, 2, 4));
        bar.surface(Surface.DARK_PANEL);
        // Clicking anywhere in the bar (not just the name text) pulls up the dropdown.
        bar.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                toggle_dropdown();
                return true;
            }
            return false;
        });

        profile_area = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        profile_area.child(bar);
        profile_area.verticalAlignment(VerticalAlignment.BOTTOM);
        profile_area.horizontalAlignment(HorizontalAlignment.RIGHT);
        profile_area.positioning(Positioning.relative(99, 97));

        return profile_area;
    }

    /** Repaints the bar label to reflect the active profile (white "None" or green name). */
    private void refresh_label(){
        if(default_active){
            profile_label.text(Component.literal("Default")).color(Color.ofArgb(0xFFFFFFFF));
        } else if(active_profile != null){
            profile_label.text(Component.literal(active_profile)).color(Color.ofArgb(0xFF40FF40));
        } else {
            profile_label.text(Component.literal("None")).color(Color.ofArgb(0xFFFFFFFF));
        }
    }

    /** Opens the dropdown if closed, closes it if open. */
    private void toggle_dropdown(){
        if(profile_dropdown != null){
            close_dropdown();
            return;
        }
        stop_naming();
        profile_dropdown = build_dropdown();
        // Insert above the bar so the list visually grows upward out of the control.
        profile_area.child(0, profile_dropdown);
        play_click();
    }

    /** Removes the dropdown from the screen if it is open. */
    private void close_dropdown(){
        if(profile_dropdown != null){
            profile_area.removeChild(profile_dropdown);
            profile_dropdown = null;
        }
    }

    /** The kind of dropdown entry. White rows (None/Default) are read-only; user rows are editable. */
    private enum RowKind { NONE, DEFAULT, USER }

    /** Builds the list: white "None", then the white "Default" (if this item has one), then user
     * profiles. White profiles carry no delete handle; user profiles do. */
    private FlowLayout build_dropdown(){
        FlowLayout list = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        list.horizontalAlignment(HorizontalAlignment.LEFT);
        list.padding(Insets.of(4));
        list.surface(Surface.DARK_PANEL);
        list.margins(Insets.bottom(2));

        list.child(dropdown_row(RowKind.NONE, null));
        if(DefaultProfiles.has(item)){
            list.child(dropdown_row(RowKind.DEFAULT, null));
        }
        for(String name : Profiles.list()){
            list.child(dropdown_row(RowKind.USER, name));
        }
        return list;
    }

    /** One dropdown entry: a red delete handle (user profiles only) plus the clickable name. */
    private UIComponent dropdown_row(RowKind kind, String name){
        FlowLayout row = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.LEFT);
        row.margins(Insets.vertical(1));

        if(kind == RowKind.USER){
            var trash = UIComponents.label(Component.literal("✕")).color(Color.ofArgb(0xFFFF5555));
            trash.shadow(true);
            trash.cursorStyle(CursorStyle.HAND);
            trash.margins(Insets.horizontal(4));
            trash.tooltip(Component.literal("Delete Profile"));
            trash.mouseEnter().subscribe(() -> trash.color(Color.ofArgb(0xFFFF0000)));
            trash.mouseLeave().subscribe(() -> trash.color(Color.ofArgb(0xFFFF5555)));
            trash.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
                if(click.button() == 0){
                    delete_profile(name);
                    return true;
                }
                return false;
            });
            row.child(trash);
        } else {
            // White profiles can't be deleted; keep their name aligned with the user rows.
            row.child(UIComponents.label(Component.literal("")).margins(Insets.horizontal(4)));
        }

        String text = switch(kind){
            case NONE -> "None";
            case DEFAULT -> "Default";
            case USER -> name;
        };
        boolean active = switch(kind){
            case NONE -> !default_active && active_profile == null;
            case DEFAULT -> default_active;
            case USER -> name.equals(active_profile);
        };
        int color = kind == RowKind.USER ? 0xFF40FF40 : 0xFFFFFFFF;

        var label = UIComponents.label(Component.literal(active ? text + " ◄" : text)).color(Color.ofArgb(color));
        label.shadow(true);
        label.cursorStyle(CursorStyle.HAND);
        label.margins(Insets.horizontal(2));
        label.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                switch(kind){
                    case NONE -> select_none();
                    case DEFAULT -> select_default();
                    case USER -> select_profile(name);
                }
                return true;
            }
            return false;
        });
        row.child(label);

        return row;
    }

    /** Reveals an inline text box for naming a new profile, seeded from the current selection. */
    private void start_naming(){
        close_dropdown();
        if(naming_row != null){
            return;
        }
        var box = UIComponents.textBox(Sizing.fixed(90));
        box.setMaxLength(32);

        var confirm = UIComponents.label(Component.literal("✔")).color(Color.ofArgb(0xFF40FF40));
        confirm.shadow(true);
        confirm.cursorStyle(CursorStyle.HAND);
        confirm.margins(Insets.horizontal(4));
        confirm.tooltip(Component.literal("Create Profile"));
        confirm.mouseDown().subscribe((MouseButtonEvent click, boolean dbl) -> {
            if(click.button() == 0){
                confirm_name(box.getValue());
                return true;
            }
            return false;
        });

        naming_row = UIContainers.horizontalFlow(Sizing.content(), Sizing.content());
        naming_row.child(box);
        naming_row.child(confirm);
        naming_row.verticalAlignment(VerticalAlignment.CENTER);
        naming_row.horizontalAlignment(HorizontalAlignment.CENTER);
        naming_row.padding(Insets.of(4));
        naming_row.surface(Surface.DARK_PANEL);
        naming_row.margins(Insets.bottom(2));

        profile_area.child(0, naming_row);
    }

    /** Hides the name-entry row if it is showing. */
    private void stop_naming(){
        if(naming_row != null){
            profile_area.removeChild(naming_row);
            naming_row = null;
        }
    }

    /** Creates a (green) user profile from the current selection, makes it active, and persists it. */
    private void confirm_name(String raw){
        String name = Profiles.sanitize(raw);
        if(name.isEmpty()){
            return;
        }
        default_active = false;
        active_profile = name;
        save_active_profile();
        stop_naming();
        refresh_label();
        play_click();
    }

    /** Selects the white "None" profile — drops any active profile without touching the selection. */
    private void select_none(){
        close_dropdown();
        default_active = false;
        active_profile = null;
        refresh_label();
        play_click();
    }

    /** Selects the read-only white "Default" profile, applying this item's bundled best-in-slot goals. */
    private void select_default(){
        close_dropdown();
        var entries = DefaultProfiles.for_item(item);
        if(entries == null){
            return; // No bundled default for this item.
        }
        default_active = true;
        active_profile = null;
        clear_all_selections();
        for(Profiles.Entry entry : entries){
            apply_selection(entry.id(), entry.level());
        }
        refresh_label();
        play_click();
    }

    /** Loads a (green) user profile, re-selecting its enchantments on the current item. */
    private void select_profile(String name){
        close_dropdown();
        default_active = false;
        active_profile = name;
        clear_all_selections();
        for(Profiles.Entry entry : Profiles.load(name)){
            apply_selection(entry.id(), entry.level());
        }

        refresh_label();
        play_click();
    }

    /** Deletes a profile from disk; if it was active, falls back to "None". */
    private void delete_profile(String name){
        Profiles.delete(name);
        if(name.equals(active_profile)){
            active_profile = null;
            refresh_label();
        }
        // Rebuild the open list so the removed row disappears immediately.
        close_dropdown();
        profile_dropdown = build_dropdown();
        profile_area.child(0, profile_dropdown);
        play_click();
    }

    /** Blue-checkmark action: overwrite the active user profile with the current on-screen selection.
     * White profiles (None/Default) are read-only, so this no-ops for them. */
    private void overwrite_active_profile(){
        if(active_profile == null){
            return; // None or read-only Default — nothing to overwrite. Use + to make a new one.
        }
        save_active_profile();
        play_click();
    }

    /** Serializes the current on-screen selection into the active profile's file. */
    private void save_active_profile(){
        if(active_profile == null){
            return;
        }
        var profile = new EnchantmaxProfile(selected_enchantments);
        List<Profiles.Entry> entries = new ArrayList<>();
        for(var ple : profile.profile){
            entries.add(new Profiles.Entry(ple.enchantment().getRegisteredName(), ple.level()));
        }
        Profiles.save(active_profile, entries);
    }

    /** Selects an enchantment at the given level by pressing its level button, as a click would. */
    private void apply_selection(String id, int level){
        EnchantSlot slot = enchant_index.get(id);
        if(slot == null){
            return;
        }
        int idx = level - slot.base_level;
        var levels = slot.level_row.children();
        if(idx < 0 || idx >= levels.size()){
            return; // Item can't reach this level (e.g. already higher) — skip it.
        }
        selected_level_button = slot.button;
        ((ButtonComponent) levels.get(idx)).onPress(new MouseButtonInfo(0, 0)); // synthetic left-click (see above)
    }

    /** Resets every currently-selected enchantment back to its base level (i.e. deselects all). */
    private void clear_all_selections(){
        ArrayList<String> ids = new ArrayList<>();
        for(var bucket_group : selected_enchantments.values()){
            for(var ench : bucket_group.getB().keySet()){
                ids.add(ench.getRegisteredName());
            }
        }
        for(String id : ids){
            EnchantSlot slot = enchant_index.get(id);
            if(slot != null){
                apply_selection(id, slot.base_level); // base level press unregisters it
            }
        }
    }

    /** Plays the standard UI click sound. */
    private void play_click(){
        if(minecraft != null && minecraft.player != null){
            minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
        }
    }






    


    

    





















    








    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
