package com.recrivenvi.ravensmod.client;

import com.recrivenvi.ravensmod.RavensMod;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class BlockEntitiesModels<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {
    private final Identifier model;
    private final Identifier texture;
    private final @Nullable Identifier animations;

    public BlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
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
