package net.nosam08.enchantmaxxing.aom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
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
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.nosam08.enchantmaxxing.EnchantifyClient;
import net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering;
import net.nosam08.enchantmaxxing.emm.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.tooltips.Enchantips;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;
import net.nosam08.enchantmaxxing.tooltips.ds.ItemStackKey;

public class AnvilMenu extends BaseOwoScreen<FlowLayout>  {

    ArrayList<BucketGroupScroller<Component>> horizontal_scrollers = new ArrayList<>();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
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
    }

    /** Starts the creation of the menu. */
    public static AnvilMenu start(){
        return new AnvilMenu();
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .surface(Surface.VANILLA_TRANSLUCENT)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.TOP);

        var active_tasks = tasks();

        var flow = Containers.verticalFlow(Sizing.content(), Sizing.content())
            .children(active_tasks)
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
        
        var padder = Containers.verticalFlow(Sizing.fill(85), Sizing.fill(85))
            .child(scroller)
            .padding(Insets.of(5))
            .surface(Surface.DARK_PANEL)
            .verticalAlignment(VerticalAlignment.CENTER)
            .horizontalAlignment(HorizontalAlignment.CENTER) //OR LEFT
            .margins(Insets.vertical(5));

        rootComponent.child(
            padder
        );
    }

    /** Creates the tasks for the main display of the menu. */
    public ArrayList<Component> tasks(){
        ArrayList<Component> active_tasks = new ArrayList<>();
        Enchantips.ACTIVE_TASKS.forEach((ItemStackKey k, EnchantmaxProfile v) -> {
            var ordering = AnvilOrdering.ordering(k, v);
            active_tasks.add(task(k, ordering));
        });
        return active_tasks;
    }

    public Component task(ItemStackKey k, String order){

        var x_button = x_button(k, client);

        var task = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .children(Arrays.asList(x_button, AnvilMenu.order(order)))
        // .padding(Insets.both(0, 2))
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.vertical(2));

        var h_scroll = BucketGroupScroller.bucket_group_scroller(task);

        horizontal_scrollers.add(h_scroll); //threatens static

        return h_scroll
        .scrollbarThiccness(0)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER);
    }

    ///Creates the button that can delete the task.
    public static Component x_button(ItemStackKey k, MinecraftClient client){
        LabelComponent label = Components.label(Text.literal("✕"));
        label.color(Color.ofArgb(0xFFFFFFFF)); // White
        label.shadow(true);
        label.cursorStyle(CursorStyle.HAND);
        
        label.mouseEnter().subscribe(() -> {
            label.color(Color.ofArgb(0xFFFF0000)); // Red
        });
        
        label.mouseLeave().subscribe(() -> {
            label.color(Color.ofArgb(0xFFFFFFFF)); // White
        });
        
        label.mouseDown().subscribe((double mouseX, double mouseY, int button) -> {
            var key = k;
            if (button == 0) { // Left click
                Enchantips.ACTIVE_TASKS.remove(key);
                client.setScreen(null);
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                // TODO maybe also add a pop up to prevent quick task losses
                return true;
            }
            return false;
        });
        
        return label;
    }
    
    /** Builds the components based on the given order. */
    public static Component order(String order){

        // int pending_pairs = 0;
        Stack<Component> last_left = new Stack<Component>();
        StringBuilder reading_buffer = new StringBuilder();

        for (int i = 0; i < order.length(); i++) {
            char c = order.charAt(i);
            // order = order.substring(1); //idea here was to skip the reading buffer but it is chill
            
            if (c == '(') {
                // pending_pairs+=1;
            } else if (c == ')') {
                // pending_pairs-=1;
                var left = last_left.pop();
                Component right;
                if(!reading_buffer.isEmpty()){
                    right = item(reading_buffer.toString());
                    reading_buffer = new StringBuilder();
                } else {
                    right = left;
                    left = last_left.pop();
                }
                var pair = item_pair(left, right);
                last_left.add(pair);
            } else if (c == ',') {
                if(!reading_buffer.isEmpty()){
                    last_left.add(item(reading_buffer.toString()));
                    reading_buffer = new StringBuilder();
                }
            } else {
                reading_buffer.append(c);
            }
        }

        if(last_left.isEmpty()){
            return item(reading_buffer.toString());
        }
        return last_left.pop();
    }


    


    /** Creates a pair of items. */
    public static Component item_pair(Component left, Component right){
        var container = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .children(Arrays.asList(left, right))
        // .padding(Insets.both(0, 2))
        .surface(Surface.PANEL_INSET) //WELLS effect 
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.both(1, 1));
        return container;
    }

    /** Creates an item component from the name of the item. */
    public static Component item(String name){
        Identifier item_id = Identifier.tryParse(name);
        ItemStack item_stack = item_id != null ?
            new ItemStack(Registries.ITEM.get(item_id), 1) :
            AnvilOrdering.deserialize_enchantment(name);

        var item = Components.item(item_stack).showOverlay(true).setTooltipFromStack(true)
        .margins(Insets.both(1, 0));
        return item;
    }










    
}
