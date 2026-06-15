package net.nosam08.enchantmaxxing.aom.graph;

import java.util.ArrayList;
import java.util.List;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.VerticalAlignment;

/**
 * Fixed-size canvas that lays the {@link OrderNode} tree out as a top-down graph: leaf items sit
 * in boxes positioned absolutely by their column/depth, and the connector lines + combine
 * junctions are drawn underneath the boxes in {@link #draw}.
 */
public class GraphContainer extends FlowLayout {

    /** Horizontal spacing per column, vertical spacing per depth row, and leaf box size. */
    private static final int H_SPACING = 54;
    private static final int V_SPACING = 60;
    private static final int BOX = 32;
    private static final int PAD = 18;

    private static final Color LINE_COLOR = Color.ofArgb(0xFFFFFFFF);
    private static final int JUNCTION_COLOR = 0xFFFFFFFF;
    private static final int JUNCTION_HALF = 3;

    private final List<OrderNode> nodes = new ArrayList<>();

    private GraphContainer(OrderNode root, int width, int height) {
        super(Sizing.fixed(width), Sizing.fixed(height), Algorithm.VERTICAL);
        OrderNode.collect(root, this.nodes);

        for (OrderNode node : this.nodes) {
            if (!node.leaf) continue;
            child(leaf_box(node).positioning(Positioning.absolute(left_of(node), top_of(node))));
        }
    }

    /** Builds a container sized to fully contain the laid-out tree. */
    public static GraphContainer of(OrderNode root) {
        int width = PAD * 2 + (int) Math.round(OrderNode.max_column(root) * H_SPACING) + BOX;
        int height = PAD * 2 + OrderNode.max_depth(root) * V_SPACING + BOX;
        return new GraphContainer(root, width, height);
    }

    /** A bordered box holding the leaf's item, with the vanilla tooltip on hover. */
    private static FlowLayout leaf_box(OrderNode node) {
        FlowLayout box = Containers.verticalFlow(Sizing.fixed(BOX), Sizing.fixed(BOX));
        box.child(Components.item(node.stack).showOverlay(true).setTooltipFromStack(true));
        box.surface(Surface.DARK_PANEL.and(Surface.outline(0xFFFFFFFF)));
        box.horizontalAlignment(HorizontalAlignment.CENTER);
        box.verticalAlignment(VerticalAlignment.CENTER);
        return box;
    }

    /** Left pixel of a leaf box, relative to the container. */
    private static int left_of(OrderNode node) {
        return PAD + (int) Math.round(node.col * H_SPACING);
    }

    private static int top_of(OrderNode node) {
        return PAD + node.depth * V_SPACING;
    }

    /** Absolute (screen-space) centre of a node, leaf or junction. */
    private int center_x(OrderNode node) {
        return this.x() + PAD + (int) Math.round(node.col * H_SPACING) + BOX / 2;
    }

    private int center_y(OrderNode node) {
        return this.y() + PAD + node.depth * V_SPACING + BOX / 2;
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        // Edges first so the boxes/junctions paint over the line ends.
        for (OrderNode node : this.nodes) {
            if (node.leaf) continue;
            int px = center_x(node);
            int py = center_y(node);
            context.drawLine(px, py, center_x(node.left), center_y(node.left), 1.5, LINE_COLOR);
            context.drawLine(px, py, center_x(node.right), center_y(node.right), 1.5, LINE_COLOR);
        }

        // Combine junctions as small square markers.
        for (OrderNode node : this.nodes) {
            if (node.leaf) continue;
            int px = center_x(node);
            int py = center_y(node);
            context.fill(px - JUNCTION_HALF, py - JUNCTION_HALF, px + JUNCTION_HALF, py + JUNCTION_HALF, JUNCTION_COLOR);
        }

        // Then the leaf boxes (children).
        super.draw(context, mouseX, mouseY, partialTicks, delta);
    }
}
