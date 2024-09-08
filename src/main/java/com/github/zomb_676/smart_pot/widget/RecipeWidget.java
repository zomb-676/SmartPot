package com.github.zomb_676.smart_pot.widget;

import com.github.zomb_676.smart_pot.extend.ICrockPotExtendedScreen;
import com.sihenzhang.crockpot.item.CrockPotItems;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class RecipeWidget extends AbstractWidget {

    private static final Font FONT = Minecraft.getInstance().font;

    public final CrockPotCookingRecipe recipe;
    public final double percent;
    public final ItemStack resultItem;


    public RecipeWidget(CrockPotCookingRecipe recipe, double percent) {
        super(0, 0, 20, 20, recipe.getResult().getDisplayName());
        this.recipe = recipe;
        this.percent = percent;
        this.resultItem = this.recipe.getResult();
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (Minecraft.getInstance().screen instanceof ICrockPotExtendedScreen screen) {
            var s = screen.smart_pot$getExtendedScreen();
            var w = s.getSelectedRecipeWidget();
            if (w != null && w.recipe == this.recipe) {
                pGuiGraphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), 0x7fffffff);
            }
        }
        pGuiGraphics.renderItem(this.resultItem, 0, 0);
        if (percent != 0.0f) {
            renderScrollingString(pGuiGraphics, this.resultItem.getHoverName(), 19, 0, this.getWidth() - 5, 8, 0xffffffff);
            var percentStr = "percent:%.1f%%".formatted(this.percent * 100);
            //TODO just hardcode now
            if (this.recipe.getResult().is(CrockPotItems.HOT_COCOA.get()) && percent < 1f) percentStr += "?";
            renderScrollingString(pGuiGraphics, Component.literal(percentStr),
                    19, 10, this.getWidth() - 5, 18, 0xffffffff);
        } else {
            renderScrollingString(pGuiGraphics, this.resultItem.getHoverName(), 19, 0, this.getWidth() - 5, 18, 0xffffffff);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {

    }

    /**
     * {@link GuiGraphics#enableScissor} should take {@link GuiGraphics#pose()} into consideration
     *
     * @see AbstractWidget#renderScrollingString
     */
    protected static void renderScrollingString(GuiGraphics pGuiGraphics, Component pText, int pMinX, int pMinY, int pMaxX, int pMaxY, int pColor) {
        int i = FONT.width(pText);
        int j = (pMinY + pMaxY - 9) / 2 + 1;
        int k = pMaxX - pMinX;
        if (i > k) {
            var point1 = new Vector4f(pMinX, pMinY, 0, 1);
            var point2 = new Vector4f(pMaxX, pMaxY, 0, 1);
            Matrix4f transMatrix = pGuiGraphics.pose().last().pose();
            transMatrix.transform(point1);
            transMatrix.transform(point2);
            int l = i - k;
            double d0 = (double) Util.getMillis() / 1000.0D;
            double d1 = Math.max((double) l * 0.5D, 3.0D);
            double d2 = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * d0 / d1)) / 2.0D + 0.5D;
            double d3 = Mth.lerp(d2, 0.0D, l);
            pGuiGraphics.enableScissor((int) point1.x, (int) point1.y, (int) point2.x, (int) point2.y);
            pGuiGraphics.drawString(FONT, pText, pMinX - (int) d3, j, pColor);
            pGuiGraphics.disableScissor();
        } else {
            pGuiGraphics.drawCenteredString(FONT, pText, (pMinX + pMaxX) / 2, j, pColor);
        }

    }
}
