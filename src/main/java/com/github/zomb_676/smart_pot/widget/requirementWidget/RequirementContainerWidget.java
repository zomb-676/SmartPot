package com.github.zomb_676.smart_pot.widget.requirementWidget;

import com.github.zomb_676.smart_pot.i18.I18Entries;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import com.sihenzhang.crockpot.recipe.cooking.requirement.IRequirement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RequirementContainerWidget extends AbstractWidget {

    private static final Font FONT = Minecraft.getInstance().font;

    private CrockPotCookingRecipe recipe;
    private List<RequirementWidget<?>> requirementWidgets;
    private boolean pass = false;
    private int potLevel = 0;

    private Component line1;
    private Component line2;

    public RequirementContainerWidget() {
        super(0, 0, 0, 0, Component.empty());
    }

    public void update(List<IRequirement> requirements, CrockPotCookingRecipe recipe, CrockPotCookingRecipe.Wrapper wrapper, int baseX, int baseY) {
        this.requirementWidgets = RequirementWidget.create(requirements, wrapper,
                baseX + RequirementWidget.SUB_INNER_PADDING, baseY + 20);

        this.pass = this.requirementWidgets.stream().map(r -> r.pass)
                .reduce(Boolean::logicalAnd).orElse(Boolean.TRUE);
        this.recipe = recipe;
        this.potLevel = wrapper.getPotLevel();

        var style = Style.EMPTY.withColor(this.potLevel >= recipe.getPotLevel() ? 0xffffff : 0xff0000);
        this.line1 = I18Entries.WIDGET_PIORITY.translate(recipe.getPriority())
                .append(Component.literal("  "))
                .append(I18Entries.WIDGET_LEVEL.translate( this.potLevel, recipe.getPotLevel())
                        .withStyle(style));
        this.line2 = I18Entries.WIDGET_WEIGHT_TIME.translate(recipe.getWeight(), recipe.getCookingTime());

        this.setPosition(baseX, baseY);
        this.setWidth(Math.max(this.requirementWidgets.stream().mapToInt(AbstractWidget::getWidth).max().orElse(1),
                Math.max(FONT.width(line1), FONT.width(line2))));
        this.setHeight(this.requirementWidgets.stream().mapToInt(AbstractWidget::getHeight).sum() + 20);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

        if (recipe == null) return;

        var baseX = getX();
        var baseY = getY();
        pGuiGraphics.drawString(FONT, line1, baseX, baseY, 0xffffffff);
        baseY += 10;
        pGuiGraphics.drawString(FONT, line2, baseX, baseY, 0xffffffff);
        baseY += 10;

        if (this.requirementWidgets != null) {
            this.requirementWidgets.forEach(w -> w.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick));
            if (requirementWidgets.size() >= 2) {
                var x = this.requirementWidgets.get(0).getX() - RequirementWidget.SUB_INNER_PADDING + RequirementWidget.SUB_GROUP_TYPE_WIDTH / 2 - 1;
                var y = this.requirementWidgets.get(0).getY() + RequirementWidget.SUB_GROUP_TYPE_HEIGHT - 1;
                var color = pass ? 0xff00ff00 : 0xffff0000;
                var yMax = this.requirementWidgets.get(this.requirementWidgets.size() - 1)
                        .getY() + RequirementWidget.ICON_SIZE / 2 + 1;
                pGuiGraphics.fill(x, y, x + RequirementWidget.INDICATE_LINE_SIZE,
                        yMax, color);
            }
            var passChar = this.pass ? "√" : "x";
            pGuiGraphics.drawString(Minecraft.getInstance().font, passChar, getX(), getY() + 20, 0Xffffffff, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {
    }

    public @Nullable RequirementWidget<?> getMouseOverWidget(int mouseX, int mouseY) {
        if (this.requirementWidgets == null) return null;
        for (var w : this.requirementWidgets) {
            var widget = w.getMouseOverWidget(mouseX, mouseY);
            if (widget != null) return widget;
        }
        return null;
    }

    public void tryRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var widget = this.getMouseOverWidget(mouseX, mouseY);
        if (widget == null) return;
        widget.renderRequirementTooltip(guiGraphics, mouseX, mouseY);
    }
}
