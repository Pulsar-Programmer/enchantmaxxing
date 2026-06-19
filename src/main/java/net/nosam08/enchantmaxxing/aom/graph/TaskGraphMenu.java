package net.nosam08.enchantmaxxing.aom.graph;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.nosam08.enchantmaxxing.aom.AnvilMenu;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;
import net.nosam08.enchantmaxxing.emm.component_data.BucketGroupScroller;

/**
 * Detail view for a single AOM task: shows the combine order as a top-down graph so you can see
 * what to combine next and the whole shape of the task. Opened from the circle button on a task
 * row; closes back to the {@link AnvilMenu}.
 */
public class TaskGraphMenu extends BaseOwoScreen<FlowLayout> {

    private final ItemStack subject;
    private final OrderString order;

    /** Horizontal scroller around the graph; ticked so its viewport tracks the window width. */
    private BucketGroupScroller<GraphContainer> h_scroll;

    public TaskGraphMenu(ItemStack subject, OrderString order) {
        this.subject = subject;
        this.order = order;
    }

    /** Keeps the horizontal viewport sized to the current window. */
    @Override
    public void tick() {
        super.tick();
        if (this.h_scroll != null) {
            this.h_scroll.tick(this.width);
        }
    }

    /** Trackpad horizontal swipes pan the graph (matching the AnvilMenu task rows). Shift + wheel
     * is already routed to the horizontal scroller by owo's native event dispatch, so we only
     * forward the horizontal axis here to avoid double-scrolling. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.h_scroll != null && net.nosam08.enchantmaxxing.EnchantifyClient.hasShiftDown()) {
            this.h_scroll.onMouseScroll(mouseX, mouseY, horizontalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

        OrderNode tree = OrderNode.parse(order);
        OrderNode.layout(tree);

        // Header: the item's name as the title, with the affordability-coloured cost right below it.
        int player_level = this.minecraft != null && this.minecraft.player != null
            ? this.minecraft.player.experienceLevel : 0;
        int cost_color = 0xFF000000 | (player_level >= order.cost ? 0x80FF20 : 0xFF6060);

        FlowLayout header = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
        header.child(UIComponents.label(subject.getHoverName()).color(Color.ofArgb(0xFFFFFFFF)).shadow(true));
        header.child(UIComponents.label(Component.literal("Cost: " + order.cost))
            .color(Color.ofArgb(cost_color)).shadow(true).margins(Insets.top(2)));
        header.horizontalAlignment(HorizontalAlignment.CENTER);
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.margins(Insets.bottom(6));

        // The graph can be wider than the screen, so wrap it in a shift-scroll horizontal scroller
        // (same pattern as the task rows in AnvilMenu) nested inside the vertical scroller. Without
        // shift held the horizontal scroller declines the event so the vertical one handles it.
        GraphContainer graph = GraphContainer.of(tree);

        this.h_scroll = BucketGroupScroller.bucket_group_scroller(graph);
        this.h_scroll.scrollbarThiccness(4).scrollbar(ScrollContainer.Scrollbar.vanilla());
        this.h_scroll.horizontalAlignment(HorizontalAlignment.CENTER);
        this.h_scroll.verticalAlignment(VerticalAlignment.CENTER);
        this.h_scroll.tick(this.width); // size the viewport now to avoid a 0-width first frame

        // Content-sized scroller (so the outer panel can centre it horizontally). Sizing.expand()
        // makes it take the height *remaining* after the header — Sizing.fill() would be 100% of the
        // panel and overflow past the bottom, cutting off the lowest graph rows.
        var scroller = UIContainers.verticalScroll(Sizing.content(), Sizing.expand(), this.h_scroll)
            .scrollbarThiccness(4)
            .scrollbar(ScrollContainer.Scrollbar.vanilla());
        scroller.horizontalAlignment(HorizontalAlignment.CENTER);
        scroller.verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout padder = UIContainers.verticalFlow(Sizing.fill(90), Sizing.fill(90));
        padder.child(header);
        padder.child(scroller);
        padder.padding(Insets.of(8));
        padder.surface(Surface.DARK_PANEL);
        padder.horizontalAlignment(HorizontalAlignment.CENTER);
        padder.verticalAlignment(VerticalAlignment.TOP);
        padder.margins(Insets.vertical(5));

        rootComponent.child(padder);
    }

    /** Escape / close returns to the task list rather than the game. */
    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(AnvilMenu.start());
        }
    }
}
