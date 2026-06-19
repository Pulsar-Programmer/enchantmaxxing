package net.nosam08.enchantmaxxing.aom.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.nosam08.enchantmaxxing.aom.actors.AnvilOrdering;
import net.nosam08.enchantmaxxing.aom.ds.OrderString;

/**
 * A node in the combine-order tree for a single AOM task.
 *
 * Leaves are the physical items/books you start with; internal nodes are anvil combines.
 * The tree is parsed from the same parenthesised {@link OrderString#ordering} that
 * {@code AnvilMenu.order} reads, and {@link #layout} assigns each node a column/depth so the
 * graph menu can draw it top-down (root combine at the top, ingredients fanning out below).
 */
public class OrderNode {
    public final boolean leaf;
    public final ItemStack stack;       // leaf only — the item shown in the box
    public final OrderNode left, right; // internal only — the two things being combined

    // Layout, filled in by layout(): col is a (possibly fractional) horizontal slot, depth is the row.
    public double col;
    public int depth;

    private OrderNode(ItemStack stack) {
        this.leaf = true;
        this.stack = stack;
        this.left = null;
        this.right = null;
    }

    private OrderNode(OrderNode left, OrderNode right) {
        this.leaf = false;
        this.stack = null;
        this.left = left;
        this.right = right;
    }

    /** Parses an order string (e.g. {@code (OBJ,(sharpness;5,unbreaking;3))}) into a tree.
     * Mirrors the stack-based walk used by {@code AnvilMenu.order}. */
    public static OrderNode parse(OrderString order_string) {
        String order = order_string.ordering;
        ItemStack obj = order_string.object;

        Deque<OrderNode> stack = new ArrayDeque<>();
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < order.length(); i++) {
            char c = order.charAt(i);
            if (c == '(') {
                // opening — nothing to do, the matching ')' builds the pair
            } else if (c == ')') {
                OrderNode left = stack.pop();
                OrderNode right;
                if (buffer.length() > 0) {
                    right = leaf(buffer.toString(), obj);
                    buffer.setLength(0);
                } else {
                    right = left;
                    left = stack.pop();
                }
                stack.push(new OrderNode(left, right));
            } else if (c == ',') {
                if (buffer.length() > 0) {
                    stack.push(leaf(buffer.toString(), obj));
                    buffer.setLength(0);
                }
            } else {
                buffer.append(c);
            }
        }

        if (stack.isEmpty()) {
            return leaf(buffer.toString(), obj);
        }
        return stack.pop();
    }

    /** Resolves a single token into a leaf node, matching {@code AnvilMenu.item}/{@code item_if}. */
    private static OrderNode leaf(String name, ItemStack obj) {
        if (name.equals("OBJ")) {
            return new OrderNode(obj);
        }
        Identifier id = Identifier.tryParse(name);
        ItemStack stack = id != null
            ? new ItemStack(BuiltInRegistries.ITEM.getValue(id), 1)
            : AnvilOrdering.deserialize_enchantment(name);
        return new OrderNode(stack);
    }

    /** Assigns depth (row) and column to every node for top-down drawing. */
    public static void layout(OrderNode root) {
        assign_depth(root, 0);
        assign_columns(root, new int[]{0});
    }

    private static void assign_depth(OrderNode node, int depth) {
        node.depth = depth;
        if (!node.leaf) {
            assign_depth(node.left, depth + 1);
            assign_depth(node.right, depth + 1);
        }
    }

    private static double assign_columns(OrderNode node, int[] next_leaf) {
        if (node.leaf) {
            node.col = next_leaf[0]++;
            return node.col;
        }
        double l = assign_columns(node.left, next_leaf);
        double r = assign_columns(node.right, next_leaf);
        node.col = (l + r) / 2.0;
        return node.col;
    }

    /** Flattens the tree into a list (pre-order) for iteration. */
    public static void collect(OrderNode node, List<OrderNode> out) {
        out.add(node);
        if (!node.leaf) {
            collect(node.left, out);
            collect(node.right, out);
        }
    }

    public static double max_column(OrderNode node) {
        if (node.leaf) return node.col;
        return Math.max(node.col, Math.max(max_column(node.left), max_column(node.right)));
    }

    public static int max_depth(OrderNode node) {
        if (node.leaf) return node.depth;
        return Math.max(max_depth(node.left), max_depth(node.right));
    }
}
