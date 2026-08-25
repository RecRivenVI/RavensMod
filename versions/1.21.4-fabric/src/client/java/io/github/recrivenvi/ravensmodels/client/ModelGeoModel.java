package io.github.recrivenvi.ravensmodels.client;

import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.ModelBlockEntity;
import io.github.recrivenvi.ravensmodels.block.ModelBlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;

import java.util.Map;

public final class ModelGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    private final Map<Block, Assets> models;

    public ModelGeoModel(Map<Block, Assets> models) {
        this.models = models;
    }

    public Assets assets(T animatable) {
        Block block;
        if (animatable instanceof ModelBlockEntity blockEntity) {
            block = blockEntity.getBlockState().getBlock();
        } else if (animatable instanceof ModelBlockItem blockItem) {
            block = blockItem.getBlock();
        } else {
            throw new IllegalArgumentException("Unsupported model owner: " + animatable.getClass().getName());
        }

        Assets assets = this.models.get(block);
        if (assets == null) {
            throw new IllegalStateException("No model registered for block " + block);
        }

        return assets;
    }

    @Override
    public ResourceLocation getModelResource(T animatable, GeoRenderer<T> renderer) {
        return assets(animatable).model();
    }

    @Override
    public ResourceLocation getTextureResource(T animatable, GeoRenderer<T> renderer) {
        return assets(animatable).texture();
    }

    @Override
    public @Nullable ResourceLocation getAnimationResource(T animatable) {
        return null;
    }

    public record Assets(ResourceLocation model, ResourceLocation texture) {
        public static Assets of(String model, String texture) {
            return new Assets(
                    ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, "geo/" + model + ".geo.json"),
                    ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, "textures/block/" + texture + ".png"));
        }
    }
}
