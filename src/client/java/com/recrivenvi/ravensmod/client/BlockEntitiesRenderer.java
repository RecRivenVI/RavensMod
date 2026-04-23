package com.recrivenvi.ravensmod.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class BlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    public BlockEntitiesRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(model);
    }
}
