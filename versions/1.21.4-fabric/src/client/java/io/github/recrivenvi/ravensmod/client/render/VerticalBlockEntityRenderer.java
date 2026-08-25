package io.github.recrivenvi.ravensmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class VerticalBlockEntityRenderer<T extends BlockEntity & GeoAnimatable> extends HorizontalBlockEntityRenderer<T> {
    public VerticalBlockEntityRenderer(GeoModel<T> model) {
        super(model);
    }

    @Override
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        rotateDownBasedAroundGeoCenter(facing, poseStack);
    }
}
