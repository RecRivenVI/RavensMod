package com.recrivenvi.ravensmod.compat;

import com.recrivenvi.ravensmod.RavensMod;
//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;*/
//?}

//? >=26.1 {
public class CompatBlockEntitiesModels<T extends GeoAnimatable> extends GeoModel<T> {
    private final Identifier model;
    private final Identifier texture;
    private final @Nullable Identifier animations;

    public CompatBlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
        this.model = Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, modelPath);
        this.texture = Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, "textures/block/" + texturePath + ".png");
        this.animations = animPath != null ? Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, animPath) : null;
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return this.model;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return this.texture;
    }

    @Override
    public Identifier getAnimationResource(T animatable) {
        return this.animations;
    }
}
//?} else {
/*public class CompatBlockEntitiesModels<T extends GeoAnimatable> extends GeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final @Nullable ResourceLocation animations;

    public CompatBlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
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
}*/
//?}
