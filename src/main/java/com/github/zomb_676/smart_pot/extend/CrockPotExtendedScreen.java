package com.github.zomb_676.smart_pot.extend;


import com.github.zomb_676.smart_pot.Config;
import com.github.zomb_676.smart_pot.CookCandidate;
import com.github.zomb_676.smart_pot.RecipeAnalyzer;
import com.github.zomb_676.smart_pot.widget.RecipeWidget;
import com.github.zomb_676.smart_pot.widget.RecipesContainerWidget;
import com.github.zomb_676.smart_pot.widget.SatisfyLevel;
import com.github.zomb_676.smart_pot.widget.VisibilityWidget;
import com.github.zomb_676.smart_pot.widget.requirementWidget.RequirementContainerWidget;
import com.google.common.collect.ImmutableTable;
import com.sihenzhang.crockpot.base.FoodCategory;
import com.sihenzhang.crockpot.block.entity.CrockPotBlockEntity;
import com.sihenzhang.crockpot.client.gui.screen.CrockPotScreen;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class CrockPotExtendedScreen {

    private static final Font FONT = Minecraft.getInstance().font;

    public final CrockPotScreen screen;

    private final List<ItemStack> recordedInputs = new ArrayList<>(4);
    private RecipeWidget selectedRecipeWidget;

    private CookCandidate cookCandidate;

    private RecipesContainerWidget recipesContainerWidget;
    private ImmutableTable<FoodCategory, ItemStack, Float> ingredientFoodValueTable;
    private RequirementContainerWidget requirementContainerWidget;
    private VisibilityWidget visibilityButton;

    public CrockPotExtendedScreen(CrockPotScreen crockPotScreen) {
        this.screen = crockPotScreen;
        IntStream.rangeClosed(1, 4).forEach($ -> recordedInputs.add(ItemStack.EMPTY));
    }

    /**
     * @return true if inputs changed
     */
    private boolean updateRecordInputs() {
        var changed = false;
        for (int i = 0; i < recordedInputs.size(); i++) {
            var recordedItem = recordedInputs.get(i);
            var current = screen.getMenu().getSlot(i).getItem();
            if (current.getItem() != recordedItem.getItem()) {
                changed = true;
                this.recordedInputs.set(i, current);
            }
        }

        return changed;
    }

    public void init() {
        this.recipesContainerWidget = this.screen.addRenderableWidget(
                new RecipesContainerWidget(screen.getGuiLeft() + 176, screen.getGuiTop(),
                        120, 100, Component.literal("name")));
        this.reCalculateRecipes();

        this.requirementContainerWidget = this.screen.addRenderableWidget(new RequirementContainerWidget());

        var button = new VisibilityWidget(screen.getGuiLeft() + 176 - 16 - Config.visibilityButtonOffsetX(),
                screen.getGuiTop() + Config.visibilityButtonOffsetY(), 16, 16);
        this.visibilityButton = this.screen.addRenderableWidget(button);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var visibility = this.visibilityButton.isVisibility();
        this.recipesContainerWidget.visible = visibility;
        this.requirementContainerWidget.visible = visibility;

        if (updateRecordInputs()) {
            this.reCalculateRecipes();
        }
        Optional.ofNullable(this.selectedRecipeWidget).map(w -> w.recipe)
                .or(() -> Optional.ofNullable(this.getHoverRecipeWidget(mouseX, mouseY)).map(w -> w.recipe))
                .ifPresent(recipe -> {
                    var baseX = this.recipesContainerWidget.getX() + 2;
                    var baseY = this.recipesContainerWidget.getY() + this.recipesContainerWidget.getHeight() + 2;
                    this.requirementContainerWidget.update(recipe.getRequirements(), recipe, this.wrapper(), baseX, baseY);
                });
    }

    private CrockPotCookingRecipe.Wrapper wrapper() {
        return new CrockPotCookingRecipe.Wrapper(this.recordedInputs, this.cookCandidate.mergedDefinition,
                ((CrockPotBlockEntity) this.screen.getMenu().getBlockEntity()).getPotLevel());
    }

    public void reCalculateRecipes() {
        var pot = ((CrockPotBlockEntity) this.screen.getMenu().getBlockEntity());
        this.cookCandidate = RecipeAnalyzer.analyze(recordedInputs, Objects.requireNonNull(pot.getLevel())
                , pot.getPotLevel(), SatisfyLevel.SMART);
        this.ingredientFoodValueTable = RecipeAnalyzer.buildFoodValueTable(this.recordedInputs, Minecraft.getInstance().level);
        this.recipesContainerWidget.setCookCandidate(this.cookCandidate);
    }

    private void renderFoodValues(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        var align = 15;
        var baseX = 0;
        var baseY = 0;
        var x = 0;
        var y = 0;
        guiGraphics.drawString(FONT, "FoodValues", baseX, baseY, 0xfffffff);
        for (var category : this.ingredientFoodValueTable.rowKeySet()) {
            var categoryItemStack = FoodCategory.getItemStack(category);
            guiGraphics.renderItem(categoryItemStack, baseX + align + x * 16, baseY);
            x++;
        }
        baseY += 16;
        for (var entry : this.ingredientFoodValueTable.columnMap().entrySet()) {
            x = 0;
            var ingredient = entry.getKey();
            guiGraphics.renderItem(ingredient, baseX, baseY + y * 16);
            for (var e : entry.getValue().entrySet()) {
                var foodCategory = e.getKey();
                guiGraphics.drawString(FONT, e.getValue().toString(), baseX + align + x * 16, baseY + y * 16 + 4, 0xffffffff);
                x++;
            }
            y++;
        }
    }

    public void setTargetRecipeItemStack(ItemStack itemStack) {
        //TODO
    }

    public @Nullable RecipeWidget getHoverRecipeWidget(int mouseX, int mouseY) {
        return this.recipesContainerWidget.getMouseHoverWidget(mouseX, mouseY);
    }

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return;
        var widget = this.recipesContainerWidget.getMouseHoverWidget((int) mouseX, (int) mouseY);
        if (widget == null) return;
        //check mouseOverOn widget is always false, as we use different coordiante
        this.selectedRecipeWidget = widget;
    }

    public RequirementContainerWidget getRequirementContainerWidget() {
        return requirementContainerWidget;
    }

    @SuppressWarnings("unused")
    public void renderBg(GuiGraphics guiGraphics, int mouseX, int mouseY) {

    }

    public RecipeWidget getSelectedRecipeWidget() {
        return selectedRecipeWidget;
    }

    public RecipesContainerWidget getRecipesContainerWidget() {
        return recipesContainerWidget;
    }

    public boolean displaySelf() {
        return visibilityButton.isVisibility();
    }
}
