package io.github.recrivenvi.ravensmodels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.recrivenvi.ravensmodels.block.ModelBlockEntity;
import net.minecraft.core.Direction;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public final class ModelBlockRenderer extends GeoBlockRenderer<ModelBlockEntity> {
    public ModelBlockRenderer(ModelGeoModel<ModelBlockEntity> model) {
        super(model);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        poseStack.translate(0, 0.5, 0);

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(90));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            default -> {
            }
        }

        poseStack.translate(0, -0.5, 0);
    }
}
