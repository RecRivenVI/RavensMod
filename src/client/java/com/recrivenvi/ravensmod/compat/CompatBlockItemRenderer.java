package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
//?} else {
/*import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;*/
//?}

public class CompatBlockItemRenderer extends GeoItemRenderer<CompatBlockItem> {
    public CompatBlockItemRenderer(GeoModel<CompatBlockItem> model) {
        super(model);
    }

    //? >=26.1 {
    @Override
    public void adjustRenderPose(RenderPassInfo<GeoRenderState> renderPass) {
        renderPass.poseStack().translate(0.5f, 0, 0.5f);
    }
    //?} else {
    /*@Override
    public void preRender(PoseStack poseStack, CompatBlockItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);

        if (!isReRender) {
            poseStack.translate(0, -0.51f, 0);
        }
    }*/
    //?}
}
