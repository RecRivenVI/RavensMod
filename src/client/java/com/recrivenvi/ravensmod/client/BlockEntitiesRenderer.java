package com.recrivenvi.ravensmod.client;

//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.AnimatedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;*/
//?}

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesModels;
import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesRenderer;

//? >=26.1 {
public class BlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable, R extends BlockEntityRenderState> extends CompatBlockEntitiesRenderer<T, R> {
    public BlockEntitiesRenderer(BlockEntityRendererProvider.Context context, CompatBlockEntitiesModels<T> model) {
        super(context, model);
    }
}
//?} else {
/*public class BlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable> extends CompatBlockEntitiesRenderer<T> {
    public BlockEntitiesRenderer(BlockEntityRendererProvider.Context context, CompatBlockEntitiesModels<T> model) {
        super(context, model);
    }
}*/
//?}
