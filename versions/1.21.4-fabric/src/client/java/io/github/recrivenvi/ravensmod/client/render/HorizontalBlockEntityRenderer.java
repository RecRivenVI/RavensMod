package io.github.recrivenvi.ravensmod.client.render;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class HorizontalBlockEntityRenderer<T extends BlockEntity & GeoAnimatable> extends GeoBlockRenderer<T> {
    public HorizontalBlockEntityRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    public void preRender(PoseStack poseStack, T animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        this.blockRenderTranslations = new Matrix4f(poseStack.last().pose());

        if (!isReRender) {
            poseStack.translate(0.5, 0, 0.5);
        }

        scaleModelForRender(this.scaleWidth, this.scaleHeight, poseStack, animatable, model, isReRender, partialTick, packedLight, packedOverlay);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        rotateNorthBasedAroundGeoCenter(facing, poseStack);
    }

    @Override
    protected Direction getFacing(T block) {
        BlockState state = block.getBlockState();

        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }

        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        if (state.hasProperty(BlockStateProperties.VERTICAL_DIRECTION)) {
            return state.getValue(BlockStateProperties.VERTICAL_DIRECTION);
        }

        return Direction.NORTH;
    }

    protected static void rotateNorthBasedAroundGeoCenter(Direction facing, PoseStack poseStack) {
        poseStack.translate(0, 0.5, 0);
        rotateFromNorth(facing, poseStack);
        poseStack.translate(0, -0.5, 0);
    }

    protected static void rotateDownBasedAroundGeoCenter(Direction facing, PoseStack poseStack) {
        poseStack.translate(0, 0.5, 0);
        rotateFromDown(facing, poseStack);
        poseStack.translate(0, -0.5, 0);
    }

    private static void rotateFromNorth(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YN.rotationDegrees(90));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            default -> {
            }
        }
    }

    private static void rotateFromDown(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(180));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.ZN.rotationDegrees(90));
            default -> {
            }
        }
    }
}
