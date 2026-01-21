package net.nosam08.enchantmaxxing.aom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.nosam08.enchantmaxxing.emm.component_data.BucketGroupScroller;
import net.nosam08.enchantmaxxing.tooltips.ds.EnchantmaxProfile;

public class AnvilMenu extends BaseOwoScreen<FlowLayout>  {

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    /** Starts the creation of the menu. */
    public static AnvilMenu start(EnchantmaxProfile profile){ //need an object also there are advanced algorithms that should be done elsewhere
        //TODO
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
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .margins(Insets.vertical(5));

        rootComponent.child(
            padder
        );
    }

    /** Creates the tasks for the main display of the menu. */
    public ArrayList<Component> tasks(){
        ArrayList<Component> active_tasks = new ArrayList<>();
        //loop over tasks
        //create a new task for each based on the DS TODO
        return active_tasks;
    }

    public static Component task(){

        var x_button = x_button();

        var container = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        // .children(children_lines)
        // .padding(Insets.both(0, 2))
        .surface(Surface.PANEL_INSET) //WELLS effect 
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(6));

        var task = Containers.horizontalFlow(Sizing.content(), Sizing.content())
        .children(new ArrayList<>(Arrays.asList(x_button, container)))
        // .padding(Insets.both(0, 2))
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(6));


        // ArrayList<Component> children = new ArrayList<>();
        // Integer size = 20;
        // for(var i = 0; i < bucketGroup.inner.size(); i++){
        //     Bucket bucket = bucketGroup.inner.get(i);
        //     var component = bucket(bucket, i, bg_index);
        //     size = Math.max(component.getRight() * 26, size); //does the removed 6 of margin ruin it?
        //     children.add(component.getLeft());
        // }

        // ArrayList<Component> children_lines = new ArrayList<>();
        // for (Component component : children) {
        //     children_lines.add(component);
        //     children_lines.add(button_box(size));
        // }
        // children_lines.removeLast();

        

        // var h_scroll = BucketGroupScroller.bucket_group_scroller(container);

        // horizontal_scrollers.add(h_scroll); TODO add horizontal scrollers to AM

        // return h_scroll
        // .scrollbarThiccness(0)
        // .verticalAlignment(VerticalAlignment.CENTER)
        // .horizontalAlignment(HorizontalAlignment.CENTER)
        // .margins(Insets.bottom(5));
        return task;
    }

    ///Creates the button that can delete the task.
    public static Component x_button(){
        //TODO finish fn body
        //TODO maybe also add a pop up to prevent quick task losses
        //TODO on hover it turns red
        return null;
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
                var pair = item_pair(left, item(reading_buffer.toString()));
                last_left.add(pair);
            } else if (c == ',') {
                if(!reading_buffer.isEmpty()){
                    last_left.add(item(reading_buffer.toString()));
                }
                reading_buffer = new StringBuilder();
            } else {
                reading_buffer.append(c);
            }
        }

        if(last_left.isEmpty()){
            return item(reading_buffer.toString());
        }
        return last_left.pop();
    }


    



    public static Component item_pair(Component left, Component right){
        //TODO
        return null;
    }

    public static Component item(String name){
        //TODO
        return null;
    }










    
}
