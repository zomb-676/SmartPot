package com.github.zomb_676.smart_pot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class IngredientTestTooltipComponent implements ClientTooltipComponent, TooltipComponent {
    private static final int DISPLAY_COUNT = 6;
    private static final int ICON_SIZE = 16;
    private static final int STRING_V_PADDING = 1;
    private static final int STRING_IMAGE_PADDING = 1;


    public final List<String> ingredients;
    private final Function<Font, Integer> textWidth;
    private final int textHeight;
    private final Function<Font, Integer> imageWidth;
    private final int imageHeight;
    private final ItemStack[] displayItems;

    public IngredientTestTooltipComponent(IngredientTest test) {
        this.ingredients = Arrays.stream(test.ingredient.values).map(RecipeAnalyzer::TagValueToString).toList();
        this.displayItems = Arrays.stream(test.ingredient.getItems())
                .filter(distinctByKey(ItemStack::getItem)).toArray(ItemStack[]::new);
        this.imageWidth = ($) -> DISPLAY_COUNT * ICON_SIZE;
        this.imageHeight = ICON_SIZE * ((int) Math.ceil(displayItems.length / 6.0f));

        this.textWidth = (f) -> ((int) this.ingredients.stream().mapToInt(f::width).max().orElse(1));
        int ingredientValueCount = test.ingredient.values.length;
        this.textHeight = ingredientValueCount * Minecraft.getInstance().font.lineHeight +
                (ingredientValueCount - 1) * STRING_V_PADDING;
    }

    private static <T, V> Predicate<T> distinctByKey(Function<? super T, ? extends V> keyExtractor) {
        Set<V> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public int getHeight() {
        return imageHeight + textHeight + STRING_IMAGE_PADDING;
    }

    @Override
    public int getWidth(@NotNull Font pFont) {
        return Math.max(imageWidth.apply(pFont), textWidth.apply(pFont));
    }

    @Override
    public void renderText(@NotNull Font pFont, int pX, int pY, @NotNull Matrix4f pMatrix, MultiBufferSource.@NotNull BufferSource pBufferSource) {
        for (String str : this.ingredients) {
            pFont.drawInBatch(str, pX, pY, -1, true, pMatrix, pBufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
            pY += pFont.lineHeight + STRING_V_PADDING;
        }
    }

    @Override
    public void renderImage(@NotNull Font pFont, int pX, int pY, @NotNull GuiGraphics pGuiGraphics) {
        pY += this.textHeight + STRING_IMAGE_PADDING;
        if (this.displayItems.length < 6) {
            for (ItemStack item : displayItems) {
                pGuiGraphics.renderItem(item, pX, pY);
                pX += ICON_SIZE;
            }
        } else {
            int i = 0;
            for (; i < 6; i++) {
                pGuiGraphics.renderItem(this.displayItems[i], pX, pY);
                pX += ICON_SIZE;
            }
            pX -= ICON_SIZE * 6;
            pY += ICON_SIZE;
            for (; i < this.displayItems.length; i++) {
                pGuiGraphics.renderItem(this.displayItems[i], pX, pY);
                pX += ICON_SIZE;
            }
        }
    }
}
