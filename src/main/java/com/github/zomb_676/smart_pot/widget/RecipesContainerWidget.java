package com.github.zomb_676.smart_pot.widget;

import com.github.zomb_676.smart_pot.Config;
import com.github.zomb_676.smart_pot.CookCandidate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RecipesContainerWidget extends AbstractScrollWidget {

    private static final int SCROLL_BAR_WIDTH = 8;

    private final List<RecipeWidget> contents = new ArrayList<>();
    @Nullable
    private RecipeWidget mouseHoverRecipeWidget;

    public RecipesContainerWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
        super(pX, pY, pWidth, pHeight, pMessage);
    }

    @Override
    protected int getInnerHeight() {
        return contents.stream().mapToInt(AbstractWidget::getHeight).sum();
    }

    @Override
    protected double scrollRate() {
        return 16;
    }

    @Override
    protected void renderContents(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.pose().translate(this.getX() + this.innerPadding(), this.getY() + this.innerPadding(), 0.0);
        this.contents.forEach(w -> {
            w.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            pGuiGraphics.pose().translate(0, w.getHeight(), 0);
        });
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    @Override
    public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        var recipeWidget = this.getMouseHoverWidget(pMouseX, pMouseY);
        if (recipeWidget != null) {
            this.mouseHoverRecipeWidget = recipeWidget;
            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, recipeWidget.resultItem, pMouseX, pMouseY);
        }

    }

    public void setCookCandidate(CookCandidate cookCandidate) {
        this.contents.clear();
        for (var recipe : cookCandidate.orderedRecipes) {
            if (Config.skipRecipe(recipe)) continue;
            var recipeWidget = new RecipeWidget(recipe, cookCandidate.pool.getOrDefault(recipe, 0d));
            recipeWidget.setWidth(this.width - this.innerPadding() * 2);
            recipeWidget.setHeight(18);
            this.contents.add(recipeWidget);
        }
        this.setScrollAmount(0);
    }

    public @Nullable RecipeWidget getMouseHoverWidget(int mouseX, int mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) return null;
        var currentY = mouseY - this.getY() - this.innerPadding() + this.scrollAmount();
        var currentX = mouseX - this.getX() - this.innerPadding();
        for (var widget : this.contents) {
            if (widget.isMouseOver(currentX, currentY)) {
                return widget;
            }
            currentY -= widget.getHeight();
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double pMouseX, double pMouseY) {
        return this.active && this.visible && pMouseX >= (double) this.getX() && pMouseY >= (double) this.getY() &&
                pMouseX < (double) (this.getX() + this.width + SCROLL_BAR_WIDTH) && pMouseY < (double) (this.getY() + this.height);
    }
}
