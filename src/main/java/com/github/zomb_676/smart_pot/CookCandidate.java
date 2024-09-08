package com.github.zomb_676.smart_pot;

import com.sihenzhang.crockpot.base.FoodValues;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CookCandidate {
    public final List<ItemStack> items;
    public final List<FoodValues> separatedDefinition;
    public final FoodValues mergedDefinition;
    public final Map<Integer, List<CrockPotCookingRecipe>> eachPriority;
    public final Map<CrockPotCookingRecipe, Double> pool;
    public final List<CrockPotCookingRecipe> orderedRecipes;

    public CookCandidate(List<ItemStack> items, List<FoodValues> separatedDefinition,
                         FoodValues mergedDefinition, Map<Integer, List<CrockPotCookingRecipe>> eachPriority,
                         Map<CrockPotCookingRecipe, Double> pool, List<CrockPotCookingRecipe> orderedRecipes) {
        this.items = items;
        this.separatedDefinition = separatedDefinition;
        this.mergedDefinition = mergedDefinition;
        this.eachPriority = eachPriority;
        this.pool = pool;
        this.orderedRecipes = orderedRecipes;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CookCandidate) obj;
        return Objects.equals(this.items, that.items) &&
                Objects.equals(this.separatedDefinition, that.separatedDefinition) &&
                Objects.equals(this.mergedDefinition, that.mergedDefinition) &&
                Objects.equals(this.eachPriority, that.eachPriority) &&
                Objects.equals(this.pool, that.pool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, separatedDefinition, mergedDefinition, eachPriority, pool);
    }

    @Override
    public String toString() {
        return "CookCandidate[" +
                "items=" + items + ", " +
                "separatedDefinition=" + separatedDefinition + ", " +
                "mergedDefinition=" + mergedDefinition + ", " +
                "eachPriority=" + eachPriority + ", " +
                "pool=" + pool + ']';
    }

}