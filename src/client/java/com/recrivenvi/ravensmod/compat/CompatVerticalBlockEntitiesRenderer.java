package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CompatVerticalBlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable, R extends BlockEntityRenderState> extends CompatBlockEntitiesRenderer<T, R> {
    public CompatVerticalBlockEntitiesRenderer(BlockEntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void tryRotateByBlockstate(RenderPassInfo renderPass, PoseStack poseStack) {
        Direction facing = (Direction) renderPass.getOrDefaultGeckolibData(DIRECTION_FACING, Direction.DOWN);
        rotateDownBasedAroundGeoCenter(facing, poseStack);
    }
}
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class CompatVerticalBlockEntitiesRenderer<T extends BlockEntity & GeoAnimatable> extends CompatBlockEntitiesRenderer<T> {
    public CompatVerticalBlockEntitiesRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        rotateDownBasedAroundGeoCenter(facing, poseStack);
    }
}*/
//?}
