package net.nosam08.enchantmaxxing.aom.graph;

import java.util.ArrayList;
import java.util.List;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.OwoUIGraphics;
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

    private static final int LINE_COLOR = 0xFFFFFFFF;
    private static final double LINE_THICKNESS = 1.5;
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
        FlowLayout box = UIContainers.verticalFlow(Sizing.fixed(BOX), Sizing.fixed(BOX));
        box.child(UIComponents.item(node.stack).showOverlay(true).setTooltipFromStack(true));
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
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        // Edges first so the boxes/junctions paint over the line ends. We draw them as rotated
        // fill() quads rather than owo's drawLine(): drawLine submits a LineElementRenderState on a
        // separate pipeline that the 1.21.6+ GUI renderer layers ABOVE the item/panel pipelines, so
        // the lines floated on top of the boxes. fill() shares the same colored-quad pipeline as the
        // junctions and obeys submission order, keeping the edges underneath the leaf boxes.
        for (OrderNode node : this.nodes) {
            if (node.leaf) continue;
            int px = center_x(node);
            int py = center_y(node);
            draw_edge(context, px, py, center_x(node.left), center_y(node.left));
            draw_edge(context, px, py, center_x(node.right), center_y(node.right));
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

    /**
     * Draws a connector as a thin rotated rectangle via {@link OwoUIGraphics#fill}, so it uses the
     * standard colored-quad GUI pipeline (same as the junctions) and stays beneath the leaf boxes,
     * instead of owo's drawLine() which renders on a pipeline layered above them.
     */
    private static void draw_edge(OwoUIGraphics context, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-3) return;

        int half = Math.max(1, (int) Math.round(LINE_THICKNESS / 2));
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate((float) x1, (float) y1);
        matrices.rotate((float) Math.atan2(dy, dx));
        // Rectangle runs along +x from the origin (x1,y1) to the far node, centred on the axis.
        context.fill(0, -half, (int) Math.round(len), half, LINE_COLOR);
        matrices.popMatrix();
    }
}
