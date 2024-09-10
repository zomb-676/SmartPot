package com.github.zomb_676.smart_pot.compat.jei;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

public class DefaultHolder<T> {
    private T value;
    private T current;

    public DefaultHolder(T value) {
        this.value = value;
        this.current = this.value;
    }

    @CanIgnoreReturnValue
    public T reset() {
        this.current = this.value;
        return this.current;
    }

    public T get() {
        return this.value;
    }

    @CanIgnoreReturnValue
    public T setValue(T value) {
        this.value = value;
        this.current = value;
        return value;
    }
}
