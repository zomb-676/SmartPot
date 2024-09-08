package com.github.zomb_676.smart_pot.widget.requirementWidget;

import com.github.zomb_676.smart_pot.FoodCategorySupplyTooltipComponent;
import com.github.zomb_676.smart_pot.IngredientTest;
import com.github.zomb_676.smart_pot.IngredientTestTooltipComponent;
import com.github.zomb_676.smart_pot.RecipeAnalyzer;
import com.google.common.collect.ImmutableList;
import com.sihenzhang.crockpot.base.FoodCategory;
import com.sihenzhang.crockpot.recipe.FoodValuesDefinition;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import com.sihenzhang.crockpot.recipe.cooking.requirement.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class RequirementWidget<T extends IRequirement> extends AbstractWidget {

    protected static final Font FONT = Minecraft.getInstance().font;

    protected static final int ICON_SIZE = 16;
    protected static final int ICON_TEXT_PADDING = 1;
    protected static final int TEXT_CENTERED_UP = ((ICON_SIZE - FONT.lineHeight) / 2) + 1;
    protected static final int REQUIREMENT_V_PADDING = 1;

    protected static final int SUB_GROUP_TYPE_WIDTH = FONT.width("&");
    protected static final int SUB_GROUP_TYPE_HEIGHT = FONT.lineHeight;

    protected static final int INDICATE_LINE_SIZE = 1;


    protected static final int SUB_INNER_PADDING_INTERNAL = 2;
    public static final int SUB_INNER_PADDING = SUB_INNER_PADDING_INTERNAL + SUB_GROUP_TYPE_WIDTH;


    protected final T requirement;

    public final boolean pass;
    protected final CrockPotCookingRecipe.Wrapper wrapper;

    protected RequirementWidget(T requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(0, 0, 0, 0, Component.empty());
        this.requirement = requirement;
        this.pass = requirement.test(wrapper);
        this.wrapper = wrapper;
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.drawLayerLine(pGuiGraphics);
        this.drawConnectSub(pGuiGraphics);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput pNarrationElementOutput) {
    }

    protected final void textRelative(GuiGraphics guiGraphics, String text, int x, int y) {
        guiGraphics.drawString(FONT, text, this.getX() + x, this.getY() + y, (this.pass ? DyeColor.GREEN : DyeColor.RED).getTextColor(), false);
    }

    abstract protected void calculateSize();

    protected void updatePosition(int x, int y) {
        this.setPosition(x, y);
    }

    protected static <T> T select(T[] array) {
        var index = (int) (System.currentTimeMillis() / 1000 % array.length);
        return array[index];
    }

    @SuppressWarnings("unchecked")
    protected static <T extends IRequirement> RequirementWidget<T> create(T requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        Object obj;
        if (requirement instanceof RequirementCategoryMax r) {
            obj = new RequirementCategoryMaxRender(r, wrapper);
        } else if (requirement instanceof RequirementCategoryMaxExclusive r) {
            obj = new RequirementCategoryMaxExclusiveRender(r, wrapper);
        } else if (requirement instanceof RequirementCategoryMin r) {
            obj = new RequirementCategoryMinRender(r, wrapper);
        } else if (requirement instanceof RequirementCategoryMinExclusive r) {
            obj = new RequirementCategoryMinExclusiveRender(r, wrapper);
        } else if (requirement instanceof RequirementMustContainIngredient r) {
            obj = new RequirementMustContainIngredientRender(r, wrapper);
        } else if (requirement instanceof RequirementMustContainIngredientLessThan r) {
            obj = new RequirementMustContainIngredientLessThanRender(r, wrapper);
        } else if (requirement instanceof RequirementCombinationAnd r) {
            obj = new RequirementCombinationAndRender(r, wrapper);
        } else if (requirement instanceof RequirementCombinationOr r) {
            obj = new RequirementCombinationOrRender(r, wrapper);
        } else throw new RuntimeException("unsupported type:" + requirement.getClass().getName());
        return (RequirementWidget<T>) obj;
    }

    public static List<RequirementWidget<?>> create(List<IRequirement> requirements, CrockPotCookingRecipe.Wrapper wrapper, int baseX, int baseY) {
        ImmutableList.Builder<RequirementWidget<?>> builder = ImmutableList.builder();
        for (var requirement : requirements) {
            var widget = RequirementWidget.create(requirement, wrapper);
            widget.calculateSize();
            widget.updatePosition(baseX, baseY);
            builder.add(widget);
            baseY += widget.getHeight();
        }
        return builder.build();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    public @Nullable RequirementWidget<?> getMouseOverWidget(int mouseX, int mouseY) {
        return this.isMouseOver(mouseX, mouseY) ? this : null;
    }

    public ItemStack getJeiItemUnderMouse() {
        return ItemStack.EMPTY;
    }

    abstract public Predicate<ItemStack> getPredicate();

    protected static Predicate<ItemStack> checkHaveFoodCategory(FoodCategory foodCategory) {
        return (itemStack) -> FoodValuesDefinition
                .getFoodValues(itemStack, Objects.requireNonNull(Minecraft.getInstance().level))
                .has(foodCategory);
    }

    protected final List<Component> createCurrentCategoryDes(FoodCategory category, float value) {
        var component = Component.empty().append(FoodCategory.getItemStack(category).getHoverName())
                .append(Component.literal("(%.1f)".formatted(value)));
        return List.of(component);
    }

    protected final Optional<TooltipComponent> createFoodCategoryTooltip(FoodCategory category) {
        return FoodCategorySupplyTooltipComponent.create(this.wrapper, category);
    }

    protected void drawLayerLine(GuiGraphics guiGraphics) {
        var x = getX() - 1;
        var y = getY() + ICON_SIZE / 2;
        var minX = x - SUB_INNER_PADDING_INTERNAL - SUB_GROUP_TYPE_WIDTH / 2;
        guiGraphics.fill(minX, y, x, y + INDICATE_LINE_SIZE, pass ? 0xff00ff00 : 0xffff0000);
    }

    protected void drawConnectSub(GuiGraphics guiGraphics) {
    }
}

class RequirementCategoryMaxRender extends RequirementWidget<RequirementCategoryMax> {

    private final float actual;
    private final FoodCategory foodCategory;
    private final ItemStack iconItem;
    private final String text;

    public RequirementCategoryMaxRender(RequirementCategoryMax requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.foodCategory = requirement.getCategory();
        this.iconItem = FoodCategory.getItemStack(this.foodCategory);
        this.actual = wrapper.getFoodValues().get(this.foodCategory);
        this.text = "%.1f<=%.1f".formatted(this.actual, this.requirement.getMax());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem(this.iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(FONT, createCurrentCategoryDes(this.foodCategory, this.actual),
                createFoodCategoryTooltip(this.foodCategory), mouseX, mouseY);
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return iconItem;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return checkHaveFoodCategory(this.foodCategory);
    }
}

class RequirementCategoryMaxExclusiveRender extends RequirementWidget<RequirementCategoryMaxExclusive> {
    private final float actual;
    private final FoodCategory foodCategory;
    private final ItemStack iconItem;
    private final String text;

    public RequirementCategoryMaxExclusiveRender(RequirementCategoryMaxExclusive requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.foodCategory = requirement.getCategory();
        this.iconItem = FoodCategory.getItemStack(this.foodCategory);
        this.actual = wrapper.getFoodValues().get(this.foodCategory);
        this.text = "%.1f<%.1f".formatted(this.actual, this.requirement.getMax());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem(this.iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(FONT, createCurrentCategoryDes(this.foodCategory, this.actual),
                createFoodCategoryTooltip(this.foodCategory), mouseX, mouseY);
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return iconItem;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return checkHaveFoodCategory(this.foodCategory);
    }
}

class RequirementCategoryMinRender extends RequirementWidget<RequirementCategoryMin> {
    private final float actual;
    private final FoodCategory foodCategory;
    private final ItemStack iconItem;
    private final String text;

    public RequirementCategoryMinRender(RequirementCategoryMin requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.foodCategory = requirement.getCategory();
        this.iconItem = FoodCategory.getItemStack(this.foodCategory);
        this.actual = wrapper.getFoodValues().get(this.foodCategory);
        this.text = "%.1f>=%.1f".formatted(this.actual, this.requirement.getMin());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem(this.iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(FONT, createCurrentCategoryDes(this.foodCategory, this.actual),
                createFoodCategoryTooltip(this.foodCategory), mouseX, mouseY);
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return iconItem;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return checkHaveFoodCategory(this.foodCategory);
    }
}

class RequirementCategoryMinExclusiveRender extends RequirementWidget<RequirementCategoryMinExclusive> {
    private final float actual;
    private final FoodCategory foodCategory;
    private final ItemStack iconItem;
    private final String text;

    public RequirementCategoryMinExclusiveRender(RequirementCategoryMinExclusive requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.foodCategory = requirement.getCategory();
        this.iconItem = FoodCategory.getItemStack(this.foodCategory);
        this.actual = wrapper.getFoodValues().get(this.foodCategory);
        this.text = "%.1f>%.1f".formatted(this.actual, this.requirement.getMin());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        pGuiGraphics.renderItem(this.iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(FONT, createCurrentCategoryDes(this.foodCategory, this.actual),
                createFoodCategoryTooltip(this.foodCategory), mouseX, mouseY);
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return iconItem;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return checkHaveFoodCategory(this.foodCategory);
    }
}

class RequirementCombinationAndRender extends RequirementWidget<RequirementCombinationAnd> {
    private final RequirementWidget<?> first;
    private final RequirementWidget<?> second;

    public RequirementCombinationAndRender(RequirementCombinationAnd requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.first = RequirementWidget.create(requirement.getFirst(), wrapper);
        this.second = RequirementWidget.create(requirement.getSecond(), wrapper);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.first.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.second.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void calculateSize() {
        this.first.calculateSize();
        this.second.calculateSize();
        this.setWidth(Math.max(this.first.getWidth(), this.second.getWidth()) + SUB_INNER_PADDING);
        this.setHeight(this.first.getHeight() + this.second.getHeight() + REQUIREMENT_V_PADDING);
    }

    @Override
    protected void updatePosition(int x, int y) {
        super.updatePosition(x, y);
        this.first.updatePosition(x + SUB_INNER_PADDING, y);
        this.second.updatePosition(x + SUB_INNER_PADDING, y + this.first.getHeight() + REQUIREMENT_V_PADDING);
    }

    @Override
    public @Nullable RequirementWidget<?> getMouseOverWidget(int mouseX, int mouseY) {
        var s = super.getMouseOverWidget(mouseX, mouseY);
        if (s == null) return null;
        var firstOver = this.first.getMouseOverWidget(mouseX, mouseY);
        if (firstOver != null) return firstOver;
        var secondOver = this.second.getMouseOverWidget(mouseX, mouseY);
        return secondOver != null ? secondOver : s;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return this.first.getPredicate().and(this.second.getPredicate());
    }

    @Override
    protected void drawConnectSub(GuiGraphics guiGraphics) {
        var x = getX();
        var y = getY();
        guiGraphics.drawString(FONT, "&", x, y, 0xffffff00);
        var color = pass ? 0xff00ff00 : 0xffff0000;
        x += SUB_GROUP_TYPE_WIDTH / 2 - 1;
        y += SUB_GROUP_TYPE_HEIGHT - 2;
        guiGraphics.fill(x, y, x + INDICATE_LINE_SIZE, this.second.getY() + ICON_SIZE / 2 + 1, color);

        x = getX();
        guiGraphics.fill(x, y, x + INDICATE_LINE_SIZE, this.first.getY() + ICON_SIZE / 2 + 1, color);

        y = this.first.getY() + ICON_SIZE / 2 + 1;
        x += INDICATE_LINE_SIZE;
        guiGraphics.fill(x - 5, y - INDICATE_LINE_SIZE, x, y, color);
    }
}

class RequirementCombinationOrRender extends RequirementWidget<RequirementCombinationOr> {
    private final RequirementWidget<?> first;
    private final RequirementWidget<?> second;

    public RequirementCombinationOrRender(RequirementCombinationOr requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.first = RequirementWidget.create(requirement.getFirst(), wrapper);
        this.second = RequirementWidget.create(requirement.getSecond(), wrapper);
    }

    @Override
    protected void calculateSize() {
        this.first.calculateSize();
        this.second.calculateSize();
        this.setWidth(Math.max(this.first.getWidth(), this.second.getWidth()) + SUB_INNER_PADDING);
        this.setHeight(this.first.getHeight() + this.second.getHeight() + REQUIREMENT_V_PADDING);
    }

    @Override
    protected void updatePosition(int x, int y) {
        super.updatePosition(x, y);
        this.first.updatePosition(x + SUB_INNER_PADDING, y);
        this.second.updatePosition(x + SUB_INNER_PADDING, y + this.first.getHeight() + REQUIREMENT_V_PADDING);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.first.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.second.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public @Nullable RequirementWidget<?> getMouseOverWidget(int mouseX, int mouseY) {
        var s = super.getMouseOverWidget(mouseX, mouseY);
        if (s == null) return null;
        var firstOver = this.first.getMouseOverWidget(mouseX, mouseY);
        if (firstOver != null) return firstOver;
        var secondOver = this.second.getMouseOverWidget(mouseX, mouseY);
        return secondOver != null ? secondOver : s;
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return this.first.getPredicate().or(this.second.getPredicate());
    }

    @Override
    protected void drawConnectSub(GuiGraphics guiGraphics) {
        var x = getX();
        var y = getY();
        guiGraphics.drawString(FONT, "|||", x, y, 0xffffff00);
        var color = pass ? 0xff00ff00 : 0xffff0000;
        x += SUB_GROUP_TYPE_WIDTH / 2 - 1;
        y += SUB_GROUP_TYPE_HEIGHT - 2;
        guiGraphics.fill(x, y, x + INDICATE_LINE_SIZE, this.second.getY() + ICON_SIZE / 2 + 1, color);

        x = getX();
        guiGraphics.fill(x, y, x + INDICATE_LINE_SIZE, this.first.getY() + ICON_SIZE / 2 + 1, color);

        y = this.first.getY() + ICON_SIZE / 2 + 1;
        x += INDICATE_LINE_SIZE;
        guiGraphics.fill(x - 5, y - INDICATE_LINE_SIZE, x, y, color);
    }
}

class RequirementMustContainIngredientRender extends RequirementWidget<RequirementMustContainIngredient> {
    private final IngredientTest test;
    private final int actual;
    private final String text;
    private final ItemStack[] ingredientItemStacks;
    private final LazyOptional<TooltipComponent> tooltip;

    public RequirementMustContainIngredientRender(RequirementMustContainIngredient requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.test = new IngredientTest(requirement.getIngredient(), wrapper);
        this.actual = this.test.passIngredientCount;
        this.text = "%d>=%d".formatted(this.actual, requirement.getQuantity());
        this.ingredientItemStacks = this.test.ingredient.getItems();
        this.tooltip = LazyOptional.of(() -> new IngredientTestTooltipComponent(test));
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        var iconItem = RequirementWidget.select(ingredientItemStacks);
        pGuiGraphics.renderItem(iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack[] ingredientItems = this.test.ingredient.getItems();
        if (ingredientItems.length > 1) {
            guiGraphics.renderTooltip(FONT, List.of(Component.literal("RequirementMustContainIngredient")),
                    tooltip.resolve(),
                    RequirementWidget.select(ingredientItemStacks),
                    mouseX, mouseY);
        } else {
            var ingredientItem = ingredientItems[0];
            var components = Screen.getTooltipFromItem(Minecraft.getInstance(), ingredientItem);
            components.add(Component.literal(RecipeAnalyzer.TagValueToString(this.test.ingredient.values[0])));
            guiGraphics.renderComponentTooltip(FONT, components,
                    mouseX, mouseY, ingredientItem);
        }
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return RequirementWidget.select(ingredientItemStacks);
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return this.requirement.getIngredient();
    }
}

class RequirementMustContainIngredientLessThanRender extends RequirementWidget<RequirementMustContainIngredientLessThan> {
    private final IngredientTest test;
    private final int actual;
    private final String text;
    private final ItemStack[] ingredientItemStacks;
    private final LazyOptional<TooltipComponent> tooltip;

    public RequirementMustContainIngredientLessThanRender(RequirementMustContainIngredientLessThan requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        super(requirement, wrapper);
        this.test = new IngredientTest(requirement.getIngredient(), wrapper);
        this.actual = this.test.passIngredientCount;
        this.text = "%d<=%d".formatted(this.actual, requirement.getQuantity());
        this.ingredientItemStacks = this.test.ingredient.getItems();
        this.tooltip = LazyOptional.of(() -> new IngredientTestTooltipComponent(test));
    }

    @Override
    protected void calculateSize() {
        this.setWidth(ICON_SIZE + ICON_TEXT_PADDING + FONT.width(text));
        this.setHeight(ICON_SIZE);
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        var iconItem = RequirementWidget.select(ingredientItemStacks);
        pGuiGraphics.renderItem(iconItem, this.getX(), this.getY());
        this.textRelative(pGuiGraphics, text, ICON_SIZE + ICON_TEXT_PADDING, TEXT_CENTERED_UP);
    }

    @Override
    public void renderRequirementTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack[] ingredientItems = this.test.ingredient.getItems();
        if (ingredientItems.length > 1) {
            guiGraphics.renderTooltip(FONT, List.of(Component.literal("RequirementMustContainIngredientLessThan")),
                    this.tooltip.resolve(),
                    RequirementWidget.select(ingredientItemStacks),
                    mouseX, mouseY);
        } else {
            var ingredientItem = ingredientItems[0];
            var components = Screen.getTooltipFromItem(Minecraft.getInstance(), ingredientItem);
            components.add(Component.literal(RecipeAnalyzer.TagValueToString(this.test.ingredient.values[0])));
            guiGraphics.renderComponentTooltip(FONT, components,
                    mouseX, mouseY, ingredientItem);
        }
    }

    @Override
    public ItemStack getJeiItemUnderMouse() {
        return RequirementWidget.select(ingredientItemStacks);
    }

    @Override
    public Predicate<ItemStack> getPredicate() {
        return this.requirement.getIngredient();
    }
}
