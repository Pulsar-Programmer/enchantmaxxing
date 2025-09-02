package net.nosam08.enchantmaxxing.emm.component_data;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.text.Text;

public class EnchantmentButton extends ButtonComponent {

    public boolean enchanted = false;
    /** Tracks the index of the bucket. */
    public int b_index;
    /** Tracks the index of the bucketgroup. */
    public int bg_index;

    public EnchantmentButton(Text message, Consumer<ButtonComponent> onPress, int b_index, int bg_index) {
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
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);
        if(enchanted){
            drawEnchantmentGlint(context);
        }
    }

    /** Creates the associated enchantment glint. */
    private void drawEnchantmentGlint(OwoUIDrawContext context) {
        // Get the button's bounds
        int x = this.x();
        int y = this.y();
        int width = this.width();
        int height = this.height();
        
        // Use vanilla's enchantment glint rendering
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // This renders the enchantment glint texture
        context.drawTexture(
            (_id) -> RenderLayer.getGlint(),
            ItemRenderer.ITEM_ENCHANTMENT_GLINT,
            x, y,           // x, y position
            0.0f, 0.0f,     // u, v texture coordinates (float)
            width, height,  // width, height to draw
            1, 1,         // texture width, height
            0x1AD4FFFF      // color (not sure this does much lol)
        );
        
        RenderSystem.disableBlend();
    }
}
