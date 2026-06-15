package com.recrivenvi.ravensmod.client;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable, R extends BlockEntityRenderState> extends GeoBlockRenderer<T, R> {
    public BlockEntitiesRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }
}
