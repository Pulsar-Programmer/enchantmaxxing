package net.nosam08.enchantmaxxing.menu.component_data;

import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;

public class BucketGroupScroller<C extends Component> extends ScrollContainer<C> {

    protected BucketGroupScroller(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(direction, horizontalSizing, verticalSizing, child);
    }

    public static <C extends Component> BucketGroupScroller<C> bucket_group_scroller(C child){

        BucketGroupScroller<C> h_scroller = new BucketGroupScroller<C>(ScrollDirection.HORIZONTAL, Sizing.fixed(0), Sizing.content(), child) ;
        
        return h_scroller;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!Screen.hasShiftDown()) {
            return false;
        }
        return super.onMouseScroll(mouseX, mouseY, amount);
    }

    /** Updates the sizing for the child. */
    public void tick(int width){
        var size = Math.min(child.fullSize().width() + 5, (int)(0.85 * width));
        horizontalSizing(Sizing.fixed(size));
    }

}
