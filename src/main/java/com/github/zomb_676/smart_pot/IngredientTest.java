package com.github.zomb_676.smart_pot;

import com.google.common.collect.ImmutableTable;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IngredientTest {
    public final Ingredient ingredient;
    public final ImmutableTable<Ingredient.Value, ItemStack, Boolean> table;
    public final Map<ItemStack, Boolean> pass;
    public final int passIngredientCount;
    private String description = null;

    public IngredientTest(@NotNull Ingredient ingredient, CrockPotCookingRecipe.Wrapper wrapper) {
        this(ingredient, IntStream.rangeClosed(0, wrapper.getContainerSize()).mapToObj(wrapper::getItem).toList());
    }

    public IngredientTest(@NotNull Ingredient ingredient, List<ItemStack> items) {
        this.ingredient = ingredient;
        ImmutableTable.Builder<Ingredient.Value, ItemStack, Boolean> builder = ImmutableTable.builder();
        for (var ingredientValue : ingredient.values) {
            for (var item : items) {
                if (item.isEmpty()) continue;

                boolean pass;
                if (ingredientValue instanceof Ingredient.ItemValue itemValue) {
                    pass = item.is(itemValue.item.getItem());
                } else if (ingredientValue instanceof Ingredient.TagValue tagValue) {
                    pass = item.is(tagValue.tag);
                } else {
                    throw new RuntimeException();
                }

                builder.put(ingredientValue, item, pass);
            }
        }
        this.table = builder.build();
        this.pass = this.table.columnKeySet().stream().collect(Collectors.toMap(Function.identity(), i -> this.table.column(i).values().stream().anyMatch(b -> b)));
        this.passIngredientCount = (int) this.pass.values().stream().filter(b -> b).count();
    }

    public String description() {
        if (description != null) return description;

        this.description = RecipeAnalyzer.tableToStringAddRow(3, this.table, RecipeAnalyzer::TagValueToString, ItemStack::getDescriptionId, (v) -> v ? "pass" : "fail", "status", (colum, $) -> this.pass.get(colum) ? "pass" : "fail");

        return this.description + "pass ingredient count: " + this.passIngredientCount + "\n";
    }
}