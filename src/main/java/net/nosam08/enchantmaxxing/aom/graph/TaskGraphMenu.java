package net.nosam08.enchantmaxxing.aom.graph;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.nosam08.enchantmaxxing.aom.AnvilMenu;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;

/**
 * Detail view for a single AOM task: shows the combine order as a top-down graph so you can see
 * what to combine next and the whole shape of the task. Opened from the circle button on a task
 * row; closes back to the {@link AnvilMenu}.
 */
public class TaskGraphMenu extends BaseOwoScreen<FlowLayout> {

    private final ItemStack subject;
    private final OrderString order;

    public TaskGraphMenu(ItemStack subject, OrderString order) {
        this.subject = subject;
        this.order = order;
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

        OrderNode tree = OrderNode.parse(order);
        OrderNode.layout(tree);

        // Header: the item's name as the title, with the affordability-coloured cost right below it.
        int player_level = this.client != null && this.client.player != null
            ? this.client.player.experienceLevel : 0;
        int cost_color = 0xFF000000 | (player_level >= order.cost ? 0x80FF20 : 0xFF6060);

        FlowLayout header = Containers.verticalFlow(Sizing.content(), Sizing.content());
        header.child(Components.label(subject.getName()).color(Color.ofArgb(0xFFFFFFFF)).shadow(true));
        header.child(Components.label(Text.literal("Cost: " + order.cost))
            .color(Color.ofArgb(cost_color)).shadow(true).margins(Insets.top(2)));
        header.horizontalAlignment(HorizontalAlignment.CENTER);
        header.verticalAlignment(VerticalAlignment.CENTER);
        header.margins(Insets.bottom(6));

        // Content-sized scroller (so the outer panel can centre it horizontally). Sizing.expand()
        // makes it take the height *remaining* after the header — Sizing.fill() would be 100% of the
        // panel and overflow past the bottom, cutting off the lowest graph rows.
        GraphContainer graph = GraphContainer.of(tree);
        var scroller = Containers.verticalScroll(Sizing.content(), Sizing.expand(), graph)
            .scrollbarThiccness(4)
            .scrollbar(ScrollContainer.Scrollbar.vanilla());
        scroller.horizontalAlignment(HorizontalAlignment.CENTER);
        scroller.verticalAlignment(VerticalAlignment.CENTER);

        FlowLayout padder = Containers.verticalFlow(Sizing.fill(90), Sizing.fill(90));
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
    public void close() {
        if (this.client != null) {
            this.client.setScreen(AnvilMenu.start());
        }
    }
}
