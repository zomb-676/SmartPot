package com.github.zomb_676.smart_pot;

import com.sihenzhang.crockpot.item.CrockPotItems;
import com.sihenzhang.crockpot.recipe.cooking.CrockPotCookingRecipe;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = SmartPot.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue displayHidden = BUILDER.comment("display hidden")
            .define("display_hidden", false);
    public static final ForgeConfigSpec.BooleanValue uiVisibility = BUILDER.comment("ui visibility")
            .define("ui_visibility", true);
    private static final ForgeConfigSpec.IntValue visibilityButtonOffsetX = BUILDER
            .comment("the visibility button's offset, by default is 7")
            .comment("anchored at right-up of the ui")
            .comment("left and down is positive direction")
            .push("visibility_button_offset")
            .comment("offset x")
            .defineInRange("offset_x", 7, Integer.MIN_VALUE, Integer.MAX_VALUE);
    private static final ForgeConfigSpec.IntValue visibilityButtonOffsetY = BUILDER
            .comment("offset y")
            .defineInRange("offset_y", 7, Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static final ForgeConfigSpec SPEC = BUILDER.pop().build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }

    public static boolean skipRecipe(CrockPotCookingRecipe recipe) {
        if (displayHidden.get()) return false;
        return recipe.getResult().is(CrockPotItems.AVAJ.get());
    }

    public static int visibilityButtonOffsetX() {
        return visibilityButtonOffsetX.get();
    }

    public static int visibilityButtonOffsetY() {
        return visibilityButtonOffsetY.get();
    }
}
