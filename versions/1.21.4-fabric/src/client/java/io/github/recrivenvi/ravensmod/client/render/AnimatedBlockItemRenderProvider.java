package io.github.recrivenvi.ravensmod.client.render;

import io.github.recrivenvi.ravensmod.client.model.RavensGeoModel;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
import io.github.recrivenvi.ravensmod.item.GeoRenderProviderFactory;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import org.jetbrains.annotations.Nullable;

public class AnimatedBlockItemRenderProvider implements GeoRenderProvider, GeoRenderProviderFactory<GeoRenderProvider> {
    private final String modelPath;
    private final String texturePath;
    private final @Nullable String animPath;
    private GeoItemRenderer<AnimatedBlockItem> renderer;

    public AnimatedBlockItemRenderProvider(String modelPath, String texturePath, @Nullable String animPath) {
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.animPath = animPath;
    }

    @Override
    public void accept(java.util.function.Consumer<? super GeoRenderProvider> consumer) {
        consumer.accept(this);
    }

    @Override
    public GeoItemRenderer<?> getGeoItemRenderer() {
        if (this.renderer == null) {
            this.renderer = new AnimatedBlockItemRenderer(new RavensGeoModel<>(this.modelPath, this.texturePath, this.animPath));
        }

        return this.renderer;
    }
}
