package net.nosam08.enchantmaxxing.menu.component_data;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantmentButton extends ButtonComponent {

    public EnchantmentButton(Text message, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        
        // TODO Auto-generated method stub
        super.renderWidget(context, mouseX, mouseY, delta);
    }

    @Override
    public ButtonComponent renderer(Renderer renderer) {
        // TODO Auto-generated method stub
        return super.renderer(renderer);
    }

    @Override
    public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(context, mouseX, mouseY, partialTicks, delta);

        drawEnchantmentGlint(context);
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
            RenderLayer::getGuiTextured,
            ItemRenderer.ITEM_ENCHANTMENT_GLINT,
            // Identifier.of("textures/misc/enchanted_item_glint.png"),
            x, y,           // x, y position
            0.0f, 0.0f,     // u, v texture coordinates (float)
            width, height,  // width, height to draw
            16, 16,         // texture width, height
            0xFFFFFFFF      // color (white with full alpha)
        );
        
        RenderSystem.disableBlend();
    }












    @Override
    public void setTooltip(Tooltip tooltip) {
        // TODO Auto-generated method stub
        super.setTooltip(tooltip);
    }

    

    @Override
    public Component tooltip(List<TooltipComponent> tooltip) {
        // TODO Auto-generated method stub
        return super.tooltip(tooltip);
    }

    @Override
    public void drawTooltip(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
        // TODO Auto-generated method stub
        super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
    }

    @Override
    public Component tooltip(@NotNull Text tooltip) {
        // TODO Auto-generated method stub
        return super.tooltip(tooltip);
    }

    
    
}
