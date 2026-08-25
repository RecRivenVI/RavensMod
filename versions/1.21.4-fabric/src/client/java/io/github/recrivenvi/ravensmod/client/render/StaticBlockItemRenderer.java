package io.github.recrivenvi.ravensmod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class StaticBlockItemRenderer extends GeoItemRenderer<AnimatedBlockItem> {
    public StaticBlockItemRenderer(GeoModel<AnimatedBlockItem> model) {
        super(model);
    }

    @Override
    public void preRender(
            PoseStack poseStack,
            AnimatedBlockItem animatable,
            BakedGeoModel model,
            @Nullable MultiBufferSource bufferSource,
            @Nullable VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            int renderColor) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);

        if (!isReRender) {
            poseStack.translate(0, -0.51f, 0);
        }
    }
}
