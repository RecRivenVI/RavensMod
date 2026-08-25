package io.github.recrivenvi.ravensmod.item;

import java.util.function.Consumer;

@FunctionalInterface
public interface GeoRenderProviderFactory<T> {
    void accept(Consumer<? super T> consumer);
}
