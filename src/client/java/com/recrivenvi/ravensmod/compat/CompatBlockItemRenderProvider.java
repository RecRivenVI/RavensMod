package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.renderer.GeoItemRenderer;
//?} else {
/*import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;*/
//?}

import org.jetbrains.annotations.Nullable;

public class CompatBlockItemRenderProvider implements GeoRenderProvider, CompatGeoRenderProviderHolder<GeoRenderProvider> {
    private final String modelPath;
    private final String texturePath;
    private final @Nullable String animPath;
    private GeoItemRenderer<CompatBlockItem> renderer;

    public CompatBlockItemRenderProvider(String modelPath, String texturePath, @Nullable String animPath) {
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
            this.renderer = new CompatBlockItemRenderer(new CompatBlockEntitiesModels<>(this.modelPath, this.texturePath, this.animPath));
        }

        return this.renderer;
    }
}
