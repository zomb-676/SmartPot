package com.github.zomb_676.smart_pot;

import com.sihenzhang.crockpot.base.FoodCategory;
import com.sihenzhang.crockpot.recipe.FoodValuesDefinition;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class FoodCategorySupplyTooltipComponent implements ClientTooltipComponent, TooltipComponent {

    private static final Font FONT = Minecraft.getInstance().font;

    private final static int ICON_SIZE = 16;
    private final static int ICON_PADDING = 1;
    private final static int TEXT_ICON_PADDING_V = 5;
    private final static int TEXT_ICON_PADDING_H = 1;

    private final ItemStack[] items;
    private final Component[] descriptions;
    private final int count;

    private final int width;

    private FoodCategorySupplyTooltipComponent(ItemStack[] items, Component[] descriptions, int count) {
        this.items = items;
        this.descriptions = descriptions;
        this.count = count;
        this.width = ICON_SIZE + TEXT_ICON_PADDING_H + Arrays.stream(descriptions).mapToInt(FONT::width).max().orElseThrow();
    }

    public static Optional<TooltipComponent> create(CrockPotCookingRecipe.Wrapper wrapper, FoodCategory foodCategory) {
        var size = wrapper.getContainerSize();
        var items = new ItemStack[size];
        var descriptions = new Component[size];
        var effectCount = 0;
        for (int i = 0; i < size; i++) {
            ItemStack item = wrapper.getItem(i);
            if (item.isEmpty()) continue;
            items[effectCount] = item;
            float foodValue = FoodValuesDefinition.getFoodValues(item, Objects.requireNonNull(Minecraft.getInstance().level)).get(foodCategory);
            descriptions[effectCount] = Component.literal("%.1f ".formatted(foodValue)).append(item.getHoverName());
            effectCount++;
        }
        if (effectCount == 0) return Optional.empty();
        return Optional.of(new FoodCategorySupplyTooltipComponent(Arrays.copyOf(items, effectCount),
                Arrays.copyOf(descriptions, effectCount), effectCount));
    }

    @Override
    public int getHeight() {
        return count * ICON_SIZE + (count - 1) * ICON_PADDING;
    }

    @Override
    public int getWidth(Font pFont) {
        return this.width;
    }

    @Override
    public void renderText(@NotNull Font pFont, int pX, int pY, Matrix4f pMatrix, MultiBufferSource.@NotNull BufferSource pBufferSource) {
        pX += ICON_SIZE + TEXT_ICON_PADDING_H;
        pY += TEXT_ICON_PADDING_V;
        for (var description : descriptions) {
            pFont.drawInBatch(description, pX, pY, 0xffffffff, true, pMatrix, pBufferSource,
                    Font.DisplayMode.NORMAL, 0, 15728880);
            pY += ICON_SIZE + ICON_PADDING;
        }
    }

    @Override
    public void renderImage(@NotNull Font pFont, int pX, int pY, @NotNull GuiGraphics pGuiGraphics) {
        for (var item : items) {
            pGuiGraphics.renderItem(item, pX, pY);
            pY += ICON_SIZE + ICON_PADDING;
        }
    }
}
