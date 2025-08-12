package net.nosam08.enchantmaxxing.menu.component_data;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;

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
        // TODO Auto-generated method stub
        super.draw(context, mouseX, mouseY, partialTicks, delta);
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
