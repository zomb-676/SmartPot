package com.github.zomb_676.smart_pot.widget;

import com.sihenzhang.crockpot.recipe.CrockPotRecipes;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import com.sihenzhang.crockpot.recipe.cooking.requirement.IRequirement;
import com.sihenzhang.crockpot.recipe.cooking.requirement.RequirementCombinationAnd;
import com.sihenzhang.crockpot.recipe.cooking.requirement.RequirementCombinationOr;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public enum SatisfyLevel {
    //use ArrayList to make the list mutable
    ALL("all") {
        @Override
        public List<CrockPotCookingRecipe> resolve(List<ItemStack> items, Level level, CrockPotCookingRecipe.Wrapper wrapper) {
            return new ArrayList<>(level.getRecipeManager().getAllRecipesFor(CrockPotRecipes.CROCK_POT_COOKING_RECIPE_TYPE.get()));
        }
    },
    ALL_PASS("all_pass") {
        @Override
        public List<CrockPotCookingRecipe> resolve(List<ItemStack> items, Level level, CrockPotCookingRecipe.Wrapper wrapper) {
            return new ArrayList<>(level.getRecipeManager().getRecipesFor(CrockPotRecipes.CROCK_POT_COOKING_RECIPE_TYPE.get(), wrapper, level));
        }
    },
    AT_LEAST_ONE("at least one") {
        @Override
        public List<CrockPotCookingRecipe> resolve(List<ItemStack> items, Level level, CrockPotCookingRecipe.Wrapper wrapper) {
            return level.getRecipeManager().getAllRecipesFor(CrockPotRecipes.CROCK_POT_COOKING_RECIPE_TYPE.get())
                    .stream().filter(recipe -> recipe.getRequirements().isEmpty() || recipe.getRequirements().stream()
                            .anyMatch(r -> recursionPassCount(r, wrapper) != 0)).collect(Collectors.toList());
        }
    },
    SMART("smart") {
        @Override
        public List<CrockPotCookingRecipe> resolve(List<ItemStack> items, Level level, CrockPotCookingRecipe.Wrapper wrapper) {
            int count = (int) items.stream().filter(i -> !i.isEmpty()).count();
            var recipes = switch (count) {
                case 0 -> ALL.resolve(items, level, wrapper);
                case 1, 2, 3 -> AT_LEAST_ONE.resolve(items, level, wrapper);
                case 4 -> ALL_PASS.resolve(items, level, wrapper);
                default -> throw new IllegalStateException("CrockPotCookingRecipe not support " + count + " items");
            };

            var comparator = Comparator.<CrockPotCookingRecipe>comparingInt(r ->
                            r.getRequirements().stream().allMatch(requirement -> requirement.test(wrapper)) ? 1 : 0).reversed()
                    .thenComparing(Comparator.<CrockPotCookingRecipe>comparingInt((r -> r.getRequirements().stream()
                            .mapToInt(requirement -> SatisfyLevel.recursionPassCount(requirement, wrapper)).sum())).reversed())
                    .thenComparing(Comparator.comparingInt(CrockPotCookingRecipe::getPriority).reversed())
                    .thenComparing(Comparator.comparing(CrockPotCookingRecipe::getWeight).reversed());
            recipes.sort(comparator);

            return recipes;
        }
    };

    public final String name;

    abstract public List<CrockPotCookingRecipe> resolve(List<ItemStack> items, Level level, CrockPotCookingRecipe.Wrapper wrapper);

    SatisfyLevel(String name) {
        this.name = name;
    }

    private static int recursionPassCount(IRequirement requirement, CrockPotCookingRecipe.Wrapper wrapper) {
        var pass = requirement.test(wrapper);
        var value = pass ? 1 : 0;
        if (requirement instanceof RequirementCombinationOr r) {
            value += recursionPassCount(r.getFirst(), wrapper);
            value += recursionPassCount(r.getSecond(), wrapper);
        } else if (requirement instanceof RequirementCombinationAnd r) {
            value += recursionPassCount(r.getFirst(), wrapper);
            value += recursionPassCount(r.getSecond(), wrapper);
        }
        return value;
    }
}