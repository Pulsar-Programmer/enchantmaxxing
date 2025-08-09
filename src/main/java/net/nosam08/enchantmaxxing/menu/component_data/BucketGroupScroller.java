package net.nosam08.enchantmaxxing.menu.component_data;

import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.gui.screen.Screen;

public class BucketGroupScroller<C extends Component> extends ScrollContainer<C> {

    

    protected BucketGroupScroller(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(direction, horizontalSizing, verticalSizing, child);
    }

    public static <C extends Component> Component bucket_group_scroller(C child){

        BucketGroupScroller<C> h_scroller = new BucketGroupScroller<C>(ScrollDirection.HORIZONTAL, Sizing.fill(50), Sizing.content(), child) ;
        
        
        var comp = h_scroller
        .scrollbarThiccness(0)
        .verticalAlignment(VerticalAlignment.CENTER)
        .horizontalAlignment(HorizontalAlignment.CENTER)
        .margins(Insets.bottom(5));

        // comp.mouseScroll();

        return comp;
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!Screen.hasShiftDown()) {
            return false;
        }
        return super.onMouseScroll(mouseX, mouseY, amount);
    }

    @Override
    public void layout(Size space) {
        super.layout(space);

        // childSize = space.width();

        // child.x

        // if (this.child.fullSize().width() < this.width()) {
        //     this.child.positioning(Positioning.relative(((this.width() - this.child.fullSize().width()) / 2), this.child.baseY()));
        // } else {
        //     this.child.positioning(Positioning.relative(0, this.child.baseY()));
        // }
    }

}
