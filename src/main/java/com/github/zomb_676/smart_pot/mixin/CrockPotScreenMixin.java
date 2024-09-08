package com.github.zomb_676.smart_pot.mixin;

import com.github.zomb_676.smart_pot.extend.CrockPotExtendedScreen;
import com.github.zomb_676.smart_pot.extend.ICrockPotExtendedScreen;
import com.sihenzhang.crockpot.client.gui.screen.CrockPotScreen;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrockPotScreen.class)
public class CrockPotScreenMixin implements ICrockPotExtendedScreen {

    @Unique
    CrockPotExtendedScreen smart_pot$extendedScreen;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void instanceInit(CallbackInfo ci) {
        this.smart_pot$extendedScreen = new CrockPotExtendedScreen((CrockPotScreen) (Object) this);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void init(CallbackInfo ci) {
        smart_pot$extendedScreen.init();
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        smart_pot$extendedScreen.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    public void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        smart_pot$extendedScreen.renderBg(guiGraphics, mouseX, mouseY);
    }

    @Override
    @Unique
    public CrockPotExtendedScreen smart_pot$getExtendedScreen() {
        return this.smart_pot$extendedScreen;
    }
}
