package com.github.zomb_676.smart_pot;

import com.github.zomb_676.smart_pot.extend.ICrockPotExtendedScreen;
import com.github.zomb_676.smart_pot.widget.requirementWidget.RequirementWidget;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Function;
import java.util.function.Predicate;


public class EventHandle {
    public static void registerEvent(IEventBus modBus, IEventBus forgeBus) {
        forgeBus.addListener(EventHandle::onScreenRenderPre);
        forgeBus.addListener(EventHandle::onScreenRenderPost);
        forgeBus.addListener(EventHandle::onMouseClicked);
        forgeBus.addListener(EventHandle::onMouseDrag);
        modBus.addListener(EventHandle::registerTooltipComponent);
    }

    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof ICrockPotExtendedScreen extendedScreen)) return;
        var mouseX = event.getMouseX();
        var mouseY = event.getMouseY();
        var screen = extendedScreen.smart_pot$getExtendedScreen();
        var widget = screen.getRequirementContainerWidget();
        if (widget == null || !widget.isMouseOver(mouseX, mouseY)) return;
        widget.tryRenderTooltip(event.getGuiGraphics(), mouseX, mouseY);

        RequirementWidget<?> mouseOverWidget = widget.getMouseOverWidget(mouseX, mouseY);
        if (mouseOverWidget != null) {
            Predicate<ItemStack> predicate = mouseOverWidget.getPredicate();
            var baseX = screen.screen.getGuiLeft();
            var baseY = screen.screen.getGuiTop();
            for (var slot : screen.screen.getMenu().slots) {
                if (slot.isActive() && predicate.test(slot.getItem())) {
                    var slotX = baseX + slot.x;
                    var slotY = baseY + slot.y;
                    event.getGuiGraphics().fill(slotX, slotY, slotX + 16, slotY + 16, 0, 0xff00ff00);
                }
            }
        }
    }

    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof ICrockPotExtendedScreen extendedScreen)) return;
        var mouseX = event.getMouseX();
        var mouseY = event.getMouseY();
        var screen = extendedScreen.smart_pot$getExtendedScreen();
        var widget = screen.getRequirementContainerWidget();
        if (widget == null || !widget.isMouseOver(mouseX, mouseY)) return;
        widget.tryRenderTooltip(event.getGuiGraphics(), mouseX, mouseY);

        RequirementWidget<?> w = widget.getMouseOverWidget(mouseX, mouseY);
        if (w != null) {
            event.getGuiGraphics().fill(w.getX(), w.getY(), w.getX() + w.getWidth(),
                    w.getY() + w.getHeight(), 0xffffffff);
        }
    }

    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Post event) {
        if (!(event.getScreen() instanceof ICrockPotExtendedScreen extendedScreen)) return;
        var screen = extendedScreen.smart_pot$getExtendedScreen();
        screen.mouseClicked(event.getMouseX(), event.getMouseY(), event.getButton());
    }

    public static void onMouseDrag(ScreenEvent.MouseDragged.Pre event) {
        if (!(event.getScreen() instanceof ICrockPotExtendedScreen extendedScreen)) return;
        var widget = extendedScreen.smart_pot$getExtendedScreen().getRecipesContainerWidget();
        var x = event.getMouseX();
        var y = event.getMouseY();
        if (widget.isMouseOver(x, y)) {
            widget.mouseDragged(x, y, event.getMouseButton(), event.getDragX(), event.getDragY());
        }
    }

    public static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IngredientTestTooltipComponent.class, Function.identity());
        event.register(FoodCategorySupplyTooltipComponent.class, Function.identity());
    }
}
