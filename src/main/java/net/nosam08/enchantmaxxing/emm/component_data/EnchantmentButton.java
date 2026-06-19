package net.nosam08.enchantmaxxing.emm.component_data;

import java.util.function.Consumer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.resources.Identifier;

public class EnchantmentButton extends ButtonComponent {

    /**
     * GUI-space enchantment glint pipeline. Reuses vanilla's POSITION_TEX_COLOR snippet
     * (vertex shader core/gui, fragment core/position_tex_color, Sampler0 plus the
     * Projection/DynamicTransforms uniforms) so it draws like any GUI texture, but swaps
     * the blend for the additive GLINT function so the glint texture glows instead of
     * being a flat ~10% alpha overlay. The snippet is exposed via enchantify.accesswidener.
     */
    private static final RenderPipeline ENCHANT_GLINT_PIPELINE = RenderPipeline
        .builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("enchantify", "pipeline/enchant_glint"))
        .withColorTargetState(new ColorTargetState(BlendFunction.GLINT))
        .build();

    public boolean enchanted = false;
    /** Tracks the index of the bucket. */
    public int b_index;
    /** Tracks the index of the bucketgroup. */
    public int bg_index;

    private float glintOffset = 0f;

    // Fully-qualified: ButtonWidget (our superclass chain) gained a nested type named Component in
    // 1.21.11, which would otherwise shadow net.minecraft.network.chat.Component for this unqualified reference.
    public EnchantmentButton(net.minecraft.network.chat.Component message, Consumer<ButtonComponent> onPress, int b_index, int bg_index) {
        super(message, onPress);
        this.b_index = b_index;
        this.bg_index = bg_index;
    }

    // @Override
    // public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        
    //     // Auto-generated method stub
    //     super.renderWidget(context, mouseX, mouseY, delta);
    // }

    // @Override
    // public ButtonComponent renderer(Renderer renderer) {
    //     // Auto-generated method stub
    //     return super.renderer(renderer);
    // }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        ///Manually animate the glint for faster speeds.
        glintOffset += delta * 0.0025; // Speed multiplier
        if (glintOffset > 16.0f) glintOffset -= 16.0f;

        if(enchanted){
            drawEnchantmentGlint(context);
        }
    }

    /** Creates the associated enchantment glint. */
    private void drawEnchantmentGlint(OwoUIGraphics context) {
        // Get the button's bounds
        int x = this.x();
        int y = this.y();
        int width = this.width();
        int height = this.height();
        
        // Draw the scrolling enchantment glint overlay. As of 1.21.6 DrawContext#drawTexture
        // takes a RenderPipeline instead of a RenderLayer function, and RenderSystem shader-color
        // state was removed in favor of the per-call color argument (the render pipeline overhaul).
        // The additive GLINT pipeline makes the texture glow rather than tint the button flat.
        context.blit(
            ENCHANT_GLINT_PIPELINE,
            ItemFeatureRenderer.ENCHANTED_GLINT_ITEM,
            x, y,           // x, y position
            glintOffset, glintOffset,     // u, v texture coordinates (float)
            width, height,  // width, height to draw
            1, 1,         // texture region width, height
            0x1AD4FFFF      // color tint (not sure this does much lol)
        );
    }
}
