package com.github.zomb_676.smart_pot;

import com.github.zomb_676.smart_pot.widget.SatisfyLevel;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.sihenzhang.crockpot.base.FoodCategory;
import com.sihenzhang.crockpot.base.FoodValues;
import com.sihenzhang.crockpot.recipe.FoodValuesDefinition;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;


public class RecipeAnalyzer {

    public static CookCandidate analyze(List<ItemStack> items, Level level, int potLevel, SatisfyLevel satisfyLevel) {
        var separatedDefinition = items.stream().map(item -> FoodValuesDefinition.getFoodValues(item, level)).toList();
        var mergedDefinition = FoodValues.merge(separatedDefinition);
        var wrapper = new CrockPotCookingRecipe.Wrapper(items, mergedDefinition, potLevel);
        var recipes = satisfyLevel.resolve(items, level, wrapper);

        var eachPriority = recipes.stream().collect(Collectors.groupingBy(CrockPotCookingRecipe::getPriority,
                () -> new TreeMap<>(Comparator.reverseOrder()), Collectors.toList()));

        var passRecipes = recipes.stream().filter(r -> r.getRequirements().stream().allMatch(rr -> rr.test(wrapper))).toList();
        var maxPriority = passRecipes.stream().mapToInt(CrockPotCookingRecipe::getPriority).max().orElseThrow();
        var pool = passRecipes.stream().filter(r -> r.getPriority() == maxPriority).toList();
        var sumWeight = (double) pool.stream().mapToInt(CrockPotCookingRecipe::getWeight).sum();
        var percent = pool.stream().collect(Collectors.toMap(Function.identity(), r -> r.getWeight() / sumWeight));

        return new CookCandidate(items, separatedDefinition, mergedDefinition, eachPriority, percent, recipes);
    }

    public static String foodValuesDescription(List<ItemStack> items, Level level) {
        var table = buildFoodValueTable(items, level);
        return tableToStringAddColum(3, table, FoodCategory::name, ItemStack::getDescriptionId, Object::toString, "total", (category, map) ->
                map.values().stream().reduce(0f, Float::sum).toString());
    }

    public static ImmutableTable<FoodCategory, ItemStack, Float> buildFoodValueTable(List<ItemStack> items, Level level) {
        var table = ImmutableTable.<FoodCategory, ItemStack, Float>builder();
        items.stream().filter(i -> !i.isEmpty()).forEach(item -> {
            var foodValue = FoodValuesDefinition.getFoodValues(item, level);
            for (FoodCategory foodCategory : FoodCategory.values()) {
                table.put(foodCategory, item, foodValue.get(foodCategory));
            }
        });
        return table.build();
    }

    public static <R, C, V> String tableToStringAddRow(int padding, Table<R, C, V> table,
                                                       Function<? super R, String> row, Function<? super C, String> colum, Function<? super V, String> value,
                                                       String additionRowName, BiFunction<? super C, Map<R, V>, String> addition
    ) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") var columTileMaxLength = table.columnKeySet().stream().mapToInt(c -> colum.apply(c).length()).max().getAsInt();
        var rowTitles = table.rowKeySet().stream().map(row).toList();
        var sb = new StringBuilder();
        sb.append(" ".repeat(columTileMaxLength + padding));
        rowTitles.forEach((rowTitle) -> sb.append(rowTitle).append(" ".repeat(padding)));
        sb.append(additionRowName).append('\n');
        table.columnMap().forEach((columElement, map) -> {
            var rowTitleIterator = rowTitles.iterator();
            sb.append(colum.apply(columElement)).append(" ".repeat(columTileMaxLength - colum.apply(columElement).length() + padding));
            map.forEach((rowElement, valueElement) -> {
                var valueStr = value.apply(valueElement);
                sb.append(valueStr);
                sb.append(" ".repeat(rowTitleIterator.next().length() + padding - valueStr.length()));
            });
            sb.append(addition.apply(columElement, map));
            sb.append('\n');
        });
        return sb.toString();
    }

    public static <R, C, V> String tableToStringAddColum(int padding, Table<R, C, V> table,
                                                         Function<? super R, String> row, Function<? super C, String> colum, Function<? super V, String> value,
                                                         String additionColumName, BiFunction<? super R, Map<C, V>, String> addition
    ) {
        @SuppressWarnings("OptionalGetWithoutIsPresent") var columTileMaxLength =
                Math.max(table.columnKeySet().stream().mapToInt(c -> colum.apply(c).length()).max().getAsInt(), additionColumName.length());
        var rowTitles = table.rowKeySet().stream().map(row).toList();
        var sb = new StringBuilder();
        sb.append(" ".repeat(columTileMaxLength + padding));
        rowTitles.forEach((rowTitle) -> sb.append(rowTitle).append(" ".repeat(padding)));
        sb.append('\n');
        table.columnMap().forEach((columElement, map) -> {
            var rowTitleIterator = rowTitles.iterator();
            sb.append(colum.apply(columElement)).append(" ".repeat(columTileMaxLength - colum.apply(columElement).length() + padding));
            map.forEach((rowElement, valueElement) -> {
                var valueStr = value.apply(valueElement);
                sb.append(valueStr);
                sb.append(" ".repeat(rowTitleIterator.next().length() + padding - valueStr.length()));
            });
            sb.append("\n");
        });
        sb.append(additionColumName).append(" ".repeat(columTileMaxLength - additionColumName.length() + padding));
        var rawTitleIterator = rowTitles.iterator();
        table.rowMap().forEach((rowElement, map) -> {
            var str = addition.apply(rowElement, map);
            sb.append(str).append(" ".repeat(rawTitleIterator.next().length() - str.length() + padding));
        });
        return sb.toString();
    }

    public static String TagValueToString(Ingredient.Value ingredientValue) {
        final String str;
        if (ingredientValue instanceof Ingredient.TagValue tagValue) {
            str = '#' + tagValue.tag.location().toString();
        } else if (ingredientValue instanceof Ingredient.ItemValue itemValue) {
            var item = itemValue.item;
            var location = ForgeRegistries.ITEMS.getKey(item.getItem());
            str = location != null ? location.toString() : item.getDescriptionId();
        } else {
            throw new RuntimeException("un-support Ingredient.Value from class " + ingredientValue.getClass().getName());
        }
        return str;
    }

}
