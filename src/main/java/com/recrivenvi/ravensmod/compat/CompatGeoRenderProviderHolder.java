package com.recrivenvi.ravensmod.compat;

import java.util.function.Consumer;

@FunctionalInterface
public interface CompatGeoRenderProviderHolder<T> {
    void accept(Consumer<? super T> consumer);
}
