package com.github.zomb_676.smart_pot.compat.jei;

import com.github.zomb_676.smart_pot.SmartPot;
import com.github.zomb_676.smart_pot.extend.CrockPotExtendedScreen;
import com.github.zomb_676.smart_pot.extend.ICrockPotExtendedScreen;
import com.github.zomb_676.smart_pot.widget.RecipeWidget;
import com.github.zomb_676.smart_pot.widget.requirementWidget.RequirementWidget;
import com.sihenzhang.crockpot.client.gui.screen.CrockPotScreen;
import com.sihenzhang.crockpot.integration.jei.CrockPotCookingRecipeCategory;
import com.sihenzhang.crockpot.inventory.CrockPotMenu;
import com.sihenzhang.crockpot.inventory.CrockPotMenuTypes;
import com.sihenzhang.crockpot.recipe.CrockPotRecipes;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@JeiPlugin
public class SmartPotJeiPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(SmartPot.MOD_ID, "jei_plugin");

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SmartPotJeiPlugin.PLUGIN_ID;
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        var handle = new IRecipeTransferHandler<CrockPotMenu, CrockPotCookingRecipe>() {

            @Override
            public @NotNull Class<? extends CrockPotMenu> getContainerClass() {
                return CrockPotMenu.class;
            }

            @Override
            public @NotNull Optional<MenuType<CrockPotMenu>> getMenuType() {
                return Optional.of(CrockPotMenuTypes.CROCK_POT_MENU_TYPE.get());
            }

            @Override
            public @NotNull RecipeType<CrockPotCookingRecipe> getRecipeType() {
                return CrockPotCookingRecipeCategory.RECIPE_TYPE;
            }

            @Override
            public @Nullable IRecipeTransferError transferRecipe(CrockPotMenu container, CrockPotCookingRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {

                return null;
            }
        };
        //transform item by recipe
        //require mod to be installed at server
        //registration.addRecipeTransferHandler(handle, handle.getRecipeType());
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGhostIngredientHandler(CrockPotScreen.class, new IGhostIngredientHandler<>() {
            @SuppressWarnings("unchecked")
            @Override
            public <I> @NotNull List<Target<I>> getTargetsTyped(@NotNull CrockPotScreen gui, @NotNull ITypedIngredient<I> ingredient, boolean doStart) {
                if (ingredient.getIngredient() instanceof ItemStack && SmartPotJeiPlugin.isCrockPotCookingResult(ingredient)) {
                    return List.of((Target<I>) new Target<ItemStack>() {
                        @Override
                        public @NotNull Rect2i getArea() {
                            return new Rect2i(0, 0, 10, 10);
                        }

                        @Override
                        public void accept(@NotNull ItemStack ingredient) {
                            if (gui instanceof ICrockPotExtendedScreen extend) {
                                extend.smart_pot$getExtendedScreen().setTargetRecipeItemStack(ingredient);
                            }
                        }
                    });
                } else {
                    return List.of();
                }
            }

            @Override
            public void onComplete() {

            }
        });
        //let jei know what ItemStack is under mouse
        registration.addGenericGuiContainerHandler(CrockPotScreen.class, new IGuiContainerHandler<CrockPotScreen>() {
            @Override
            public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(@NotNull CrockPotScreen screen, double mouseX, double mouseY) {
                if (!(screen instanceof ICrockPotExtendedScreen extendedScreen)) return Optional.empty();
                CrockPotExtendedScreen extendScreen = extendedScreen.smart_pot$getExtendedScreen();
                return Optional.ofNullable(extendScreen.getRequirementContainerWidget())
                        .map(w -> w.getMouseOverWidget((int) mouseX, (int) mouseY))
                        .map(w -> ClickableIngredient.create(w, registration.getJeiHelpers()))
                        .or(() -> Optional.ofNullable(extendScreen.getHoverRecipeWidget((int) mouseX, (int) mouseY))
                                .map(widget -> ClickableIngredient.create(widget, registration.getJeiHelpers())))
                        .map(Function.identity());
            }
        });
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static boolean isCrockPotCookingResult(ITypedIngredient<?> ingredient) {
        var item = ingredient.getItemStack().get().getItem();
        var level = Objects.requireNonNull(Minecraft.getInstance().level);
        return level.getRecipeManager().getAllRecipesFor(CrockPotRecipes.CROCK_POT_COOKING_RECIPE_TYPE.get()).stream().anyMatch(r -> r.getResultItem(level.registryAccess()).is(item));
    }

    private static class ClickableIngredient implements IClickableIngredient<ItemStack> {
        public final ITypedIngredient<ItemStack> ingredient;
        public final Rect2i rect2i;

        private ClickableIngredient(ITypedIngredient<ItemStack> ingredient, Rect2i rect2i) {
            this.ingredient = ingredient;
            this.rect2i = rect2i;
        }

        private ClickableIngredient(ITypedIngredient<ItemStack> ingredient, LayoutElement element) {
            this(ingredient, new Rect2i(element.getX(), element.getY(), element.getWidth(), element.getHeight()));
        }

        @NotNull
        private static ClickableIngredient create(RecipeWidget widget, IJeiHelpers jeiHelpers) {
            var ingredient = jeiHelpers.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, widget.resultItem).orElseThrow();
            return new ClickableIngredient(ingredient, widget);
        }

        @Nullable
        private static ClickableIngredient create(RequirementWidget<?> widget, IJeiHelpers jeiHelpers) {
            var item = widget.getJeiItemUnderMouse();
            if (item.isEmpty()) return null;
            var ingredient = jeiHelpers.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, item).orElseThrow();
            return new ClickableIngredient(ingredient, widget);
        }

        @Override
        public @NotNull ITypedIngredient<ItemStack> getTypedIngredient() {
            return ingredient;
        }

        @Override
        public @NotNull Rect2i getArea() {
            return rect2i;
        }
    }
}
