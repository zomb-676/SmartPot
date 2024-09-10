package com.github.zomb_676.smart_pot.i18;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

@FunctionalInterface
public interface TranslatableEntry extends Function<Object[], MutableComponent> {
    MutableComponent translate(Object... args);

    @Override
    default MutableComponent apply(Object[] objects) {
        return translate(objects);
    }

    static TranslatableEntry create(String key) {
        return args -> Component.translatable(key, args);
    }
}
