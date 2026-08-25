package io.github.recrivenvi.ravensmod.client.model;

import io.github.recrivenvi.ravensmod.RavensMod;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

public final class StaticGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;

    public StaticGeoModel(String model, String texture) {
        this.model = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "geo/" + model + ".geo.json");
        this.texture = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "textures/block/" + texture + ".png");
    }

    @Override
    public ResourceLocation getModelResource(T animatable, GeoRenderer<T> renderer) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable, GeoRenderer<T> renderer) {
        return this.texture;
    }

    @Override
    public @Nullable ResourceLocation getAnimationResource(T animatable) {
        return null;
    }
}
