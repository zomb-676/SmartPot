package com.github.zomb_676.smart_pot.widget;import com.github.zomb_676.smart_pot.Config;
import com.github.zomb_676.smart_pot.i18.I18Entries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class VisibilityWidget extends AbstractWidget {

    private static final ResourceLocation ICON_LOCATION = new ResourceLocation("crockpot", "textures/item/book.png");
    private static final int EXTEND = 2;

    public VisibilityWidget(int pX, int pY, int pWidth, int pHeight) {
        super(pX - EXTEND, pY - EXTEND, pWidth + EXTEND * 2, pHeight + EXTEND * 2, Component.empty());
    }

    @Override
    protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.blitWithBorder(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + 20, this.width, this.height, 200, 20, 2, 3, 2, 2);

        pGuiGraphics.blit(ICON_LOCATION, getX() + EXTEND, getY() + EXTEND, 0, 0, 16, 16, 16, 16);
        if (isMouseOver(pMouseX, pMouseY)) {
            pGuiGraphics.renderTooltip(Minecraft.getInstance().font,
                    I18Entries.TOOLTIP_SWITCH_VISIBILITY_STATE, pMouseX, pMouseY);
        }
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        var visibility = Config.uiVisibility;
        visibility.set(!visibility.get());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public boolean isVisibility() {
        return Config.uiVisibility.get();
    }
}
