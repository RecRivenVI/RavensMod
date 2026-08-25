package io.github.recrivenvi.ravensmod.client.model;

import io.github.recrivenvi.ravensmod.RavensMod;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class RavensGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final @Nullable ResourceLocation animations;

    public RavensGeoModel(String modelPath, String texturePath, @Nullable String animPath) {
        this.model = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "geo/" + modelPath + ".geo.json");
        this.texture = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "textures/block/" + texturePath + ".png");
        this.animations = animPath != null ? ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "animations/" + animPath + ".animation.json") : null;
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
    public ResourceLocation getAnimationResource(T animatable) {
        return this.animations;
    }
}
