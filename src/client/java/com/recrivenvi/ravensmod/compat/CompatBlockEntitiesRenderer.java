package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompatBlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable, R extends BlockEntityRenderState> extends GeoBlockRenderer<T, R> {
    public CompatBlockEntitiesRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }
}
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.AnimatedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompatBlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    public CompatBlockEntitiesRenderer(BlockEntityRendererProvider.Context context, AnimatedGeoModel<T> model) {
        super(context, model);
    }
}*/
//?}
