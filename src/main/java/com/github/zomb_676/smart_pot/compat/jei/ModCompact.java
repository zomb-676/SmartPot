package com.github.zomb_676.smart_pot.compat.jei;

import java.util.function.BooleanSupplier;

public class ModCompact {
    public static DefaultHolder<BooleanSupplier> isJeiOverlayDisplayed = new DefaultHolder<>(() -> false);
}