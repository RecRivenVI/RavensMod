package io.github.recrivenvi.ravensmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.recrivenvi.ravensmod.block.OmnidirectionalModelBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class VerticalBlockEntityRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    public VerticalBlockEntityRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    protected Direction getFacing(T block) {
        return block.getBlockState().getValue(OmnidirectionalModelBlock.FACING);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        poseStack.translate(0, 0.5, 0);

        switch (facing) {
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90));
            default -> {
            }
        }

        poseStack.translate(0, -0.5, 0);
    }
}
