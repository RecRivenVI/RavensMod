package com.recrivenvi.ravensmod.compat;

import com.recrivenvi.ravensmod.RavensMod;
//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class CompatBlockEntitiesModels<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {
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
/*import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.AnimatedGeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class CompatBlockEntitiesModels<T extends BlockEntity & GeoAnimatable> extends AnimatedGeoModel<T> {
    private final ResourceLocation model;
    private final ResourceLocation texture;
    private final @Nullable ResourceLocation animations;

    public CompatBlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
        this.model = new ResourceLocation(RavensMod.MOD_ID, "geo/" + modelPath + ".geo.json");
        this.texture = new ResourceLocation(RavensMod.MOD_ID, "textures/block/" + texturePath + ".png");
        this.animations = animPath != null ? new ResourceLocation(RavensMod.MOD_ID, "animations/" + animPath + ".animation.json") : null;
    }

    @Override
    public ResourceLocation getModelResource(T animatable) {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return this.texture;
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return this.animations;
    }
}*/
//?}
