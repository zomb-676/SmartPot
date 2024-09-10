package com.github.zomb_676.smart_pot.i18;

import com.github.zomb_676.smart_pot.SmartPot;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class I18Entries {
    private I18Entries() {
        throw new IllegalStateException();
    }

    private static MutableComponent createKey(String key) {
        return Component.translatable(key);
    }

    private static TranslatableEntry create(String key) {
        return TranslatableEntry.create(key);
    }

    private static String createNoneArgs(String prefix, String suffix) {
        return "%s.%s.%s".formatted(prefix, SmartPot.MOD_ID, suffix);
    }

    private static String tooltip(String str) {
        return createNoneArgs("tooltip",str);
    }

    private static String widget(String str) {
        return createNoneArgs("widget", str);
    }

    public static final MutableComponent TOOLTIP_SWITCH_VISIBILITY_STATE = createKey(tooltip("switch_visibility_state"));
    public static final MutableComponent TOOLTIP_REQUIRED_CONTAIN_INGREDIENT_GREATER = createKey(tooltip("require_contain_ingredient_greater"));
    public static final MutableComponent TOOLTIP_REQUIRED_CONTAIN_INGREDIENT_LESS = createKey(tooltip("require_contain_ingredient_less"));

    public static final TranslatableEntry WIDGET_PIORITY = create(widget("piority"));
    public static final TranslatableEntry WIDGET_LEVEL = create(widget("level"));
    public static final TranslatableEntry WIDGET_WEIGHT_TIME = create(widget("weight_time"));
    public static final TranslatableEntry WIDGET_PERCENT_DISPLAY = create(widget("percent_display"));
}
