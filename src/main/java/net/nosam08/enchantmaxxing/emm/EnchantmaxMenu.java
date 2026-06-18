package net.nosam08.enchantmaxxing.emm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.core.Positioning;
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
import net.nosam08.enchantmaxxing.profiles.Profiles;
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

    /// --- Profiles ---
    /** The currently loaded profile, or {@code null} when "None" is selected. */
    private String active_profile = null;
    /** Suppresses per-edit auto-save (and selection sounds) while a profile is being applied in bulk. */
    private boolean applying = false; //consider removing this
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
        final ParentComponent level_row;
        EnchantSlot(EnchantmentButton button, int base_level, ParentComponent level_row) {
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

        // Floats over the bottom-right corner; its dropdown grows upward out of the bar.
        rootComponent.child(profile_selector());

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
        
        EnchantmentButton btn = new EnchantmentButton(name, b -> {
            on_enchant_click(b, horizontal);
        }, b_index, bg_index);
        btn.margins(Insets.horizontal(3));

        var head = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .child(btn)
        .child(horizontal)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);

        // Index this enchantment so a loaded profile can re-select it without a real mouse click.
        enchant_index.put(enchantment.getIdAsString(), new EnchantSlot(btn, level, horizontal));

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
            unregister_enchantment(ench, level);

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

    /** Unregister level in output map. */
    public void unregister_enchantment(RegistryEntry<Enchantment> ench, int level){
        var present = selected_enchantments.getOrDefault(selected_level_button.bg_index, new Pair<>(selected_level_button.b_index, new HashMap<>()));
        present.getRight().remove(ench);
    }

    /** Makes the button fancy. */
    public void animate_button(EnchantmentButton button){
        if(!applying){
            client.player.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
        }
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
    public Component profile_selector(){
        profile_label = Components.label(Text.literal("None")).color(Color.ofArgb(0xFFFFFFFF));
        profile_label.shadow(true);
        profile_label.cursorStyle(CursorStyle.HAND);
        profile_label.margins(Insets.horizontal(6));
        profile_label.tooltip(Text.literal("Select Profile"));
        profile_label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                toggle_dropdown();
                return true;
            }
            return false;
        });

        var plus = Components.label(Text.literal("+")).color(Color.ofArgb(0xFF40FF40));
        plus.shadow(true);
        plus.cursorStyle(CursorStyle.HAND);
        plus.margins(Insets.of(0, 0, 4, 4));
        plus.tooltip(Text.literal("Add Profile"));
        plus.mouseEnter().subscribe(() -> plus.color(Color.ofArgb(0xFF80FF80)));
        plus.mouseLeave().subscribe(() -> plus.color(Color.ofArgb(0xFF40FF40)));
        plus.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                start_naming();
                return true;
            }
            return false;
        });

        // Blue checkmark: overwrite the active profile with the current on-screen selection.
        var save = Components.label(Text.literal("✔")).color(Color.ofArgb(0xFF55AAFF));
        save.shadow(true);
        save.cursorStyle(CursorStyle.HAND);
        save.margins(Insets.of(0, 0, 2, 4));
        save.tooltip(Text.literal("Save Profile"));
        save.mouseEnter().subscribe(() -> save.color(Color.ofArgb(0xFF80C8FF)));
        save.mouseLeave().subscribe(() -> save.color(Color.ofArgb(0xFF55AAFF)));
        save.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                overwrite_active_profile();
                return true;
            }
            return false;
        });

        var bar = Containers.horizontalFlow(Sizing.content(), Sizing.content())
            .child(plus)
            .child(save)
            .child(button_box(16))
            .child(profile_label);
        bar.verticalAlignment(VerticalAlignment.CENTER);
        bar.horizontalAlignment(HorizontalAlignment.CENTER);
        bar.padding(Insets.of(4, 4, 2, 4));
        bar.surface(Surface.DARK_PANEL);
        // Clicking anywhere in the bar (not just the name text) pulls up the dropdown.
        bar.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                toggle_dropdown();
                return true;
            }
            return false;
        });

        profile_area = Containers.verticalFlow(Sizing.content(), Sizing.content());
        profile_area.child(bar);
        profile_area.verticalAlignment(VerticalAlignment.BOTTOM);
        profile_area.horizontalAlignment(HorizontalAlignment.RIGHT);
        profile_area.positioning(Positioning.relative(99, 97));

        return profile_area;
    }

    /** Repaints the bar label to reflect the active profile (white "None" or green name). */
    private void refresh_label(){
        if(active_profile == null){
            profile_label.text(Text.literal("None")).color(Color.ofArgb(0xFFFFFFFF));
        } else {
            profile_label.text(Text.literal(active_profile)).color(Color.ofArgb(0xFF40FF40));
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

    /** Builds the list of selectable profiles, "None" first, each saved profile with a delete handle. */
    private FlowLayout build_dropdown(){
        FlowLayout list = Containers.verticalFlow(Sizing.content(), Sizing.content());
        list.horizontalAlignment(HorizontalAlignment.LEFT);
        list.padding(Insets.of(4));
        list.surface(Surface.DARK_PANEL);
        list.margins(Insets.bottom(2));

        list.child(dropdown_row(null));
        for(String name : Profiles.list()){
            list.child(dropdown_row(name));
        }
        return list;
    }

    /** One dropdown entry: a red delete handle (omitted for "None") plus the clickable profile name. */
    private Component dropdown_row(String name){
        FlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        row.verticalAlignment(VerticalAlignment.CENTER);
        row.horizontalAlignment(HorizontalAlignment.LEFT);
        row.margins(Insets.vertical(1));

        if(name != null){
            var trash = Components.label(Text.literal("✕")).color(Color.ofArgb(0xFFFF5555));
            trash.shadow(true);
            trash.cursorStyle(CursorStyle.HAND);
            trash.margins(Insets.horizontal(4));
            trash.tooltip(Text.literal("Delete Profile"));
            trash.mouseEnter().subscribe(() -> trash.color(Color.ofArgb(0xFFFF0000)));
            trash.mouseLeave().subscribe(() -> trash.color(Color.ofArgb(0xFFFF5555)));
            trash.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if(button == 0){
                    delete_profile(name);
                    return true;
                }
                return false;
            });
            row.child(trash);
        } else {
            // Keep the "None" name aligned with the others that carry a delete handle.
            row.child(Components.label(Text.literal("")).margins(Insets.horizontal(4)));
        }

        boolean active = name == null ? active_profile == null : name.equals(active_profile);
        int color = name == null ? 0xFFFFFFFF : 0xFF40FF40;
        var label = Components.label(Text.literal(name == null ? "None" : name)).color(Color.ofArgb(color));
        label.shadow(true);
        label.cursorStyle(CursorStyle.HAND);
        label.margins(Insets.horizontal(2));
        if(active){
            label.text(Text.literal((name == null ? "None" : name) + " ◄"));
        }
        label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                select_profile(name);
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
        var box = Components.textBox(Sizing.fixed(90));
        box.setMaxLength(32);

        var confirm = Components.label(Text.literal("✔")).color(Color.ofArgb(0xFF40FF40));
        confirm.shadow(true);
        confirm.cursorStyle(CursorStyle.HAND);
        confirm.margins(Insets.horizontal(4));
        confirm.tooltip(Text.literal("Create Profile"));
        confirm.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if(button == 0){
                confirm_name(box.getText());
                return true;
            }
            return false;
        });

        naming_row = Containers.horizontalFlow(Sizing.content(), Sizing.content());
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

    /** Creates a profile from the current selection, makes it active, and persists it. */
    private void confirm_name(String raw){
        String name = Profiles.sanitize(raw);
        if(name.isEmpty()){
            return;
        }
        active_profile = name;
        save_active_profile();
        stop_naming();
        refresh_label();
        play_click();
    }

    /** Loads a profile (or clears to "None"), re-selecting its enchantments on the current item. */
    private void select_profile(String name){
        close_dropdown();
        if(name == null){
            active_profile = null;
            refresh_label();
            play_click();
            return;
        }

        active_profile = name;
        applying = true;
        clear_all_selections();
        for(Profiles.Entry entry : Profiles.load(name)){
            apply_selection(entry.id(), entry.level());
        }
        applying = false;

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

    /** Blue-checkmark action: overwrite the active profile with the current on-screen selection. */
    private void overwrite_active_profile(){
        if(active_profile == null){
            return; // No profile loaded to save into — create one with + first.
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
            entries.add(new Profiles.Entry(ple.enchantment.getIdAsString(), ple.level));
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
        ((ButtonComponent) levels.get(idx)).onPress();
    }

    /** Resets every currently-selected enchantment back to its base level (i.e. deselects all). */
    private void clear_all_selections(){
        ArrayList<String> ids = new ArrayList<>();
        for(var bucket_group : selected_enchantments.values()){
            for(var ench : bucket_group.getRight().keySet()){
                ids.add(ench.getIdAsString());
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
        if(client != null && client.player != null){
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
        }
    }






    


    

    





















    








    
    /** Where all the UI happens based on the {@code MenuInstructions}. */
    public static Screen afterfuse(MenuInstructions instructions){
        return null; //TODO
    }
}
