package io.github.recrivenvi.ravensmodels.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.block.ModelBlockEntity;
import io.github.recrivenvi.ravensmodels.block.ModelBlockItem;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.Map;

public final class RavensModelsClient implements ClientModInitializer {
    private static final Map<Block, ModelGeoModel.Assets> MODELS = Map.of(
            ModContent.SHATTERED_THRONE, ModelGeoModel.Assets.of("shattered_throne", "mirror_block"),
            ModContent.G3_ROUND_CORNER, ModelGeoModel.Assets.of("g3_round_corner", "white_block"),
            ModContent.G3_CONNECTOR, ModelGeoModel.Assets.of("g3_connector", "white_block"));

    @Override
    public void onInitializeClient() {
        SphereBakedModel.register();

        ModelGeoModel<ModelBlockEntity> blockModel = new ModelGeoModel<>(MODELS);
        BlockEntityRenderers.register(ModContent.MODEL_BLOCK_ENTITY_TYPE, context -> new ModelBlockRenderer(blockModel));

        GeoRenderProvider itemRenderProvider = new GeoRenderProvider() {
            private GeoItemRenderer<?> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BlockOriginItemRenderer();
                }

                return this.renderer;
            }
        };

        for (Block block : MODELS.keySet()) {
            ((ModelBlockItem)block.asItem()).setRenderProvider(itemRenderProvider);
        }
    }

    private static final class BlockOriginItemRenderer extends GeoItemRenderer<ModelBlockItem> {
        private BlockOriginItemRenderer() {
            super(new ModelGeoModel<>(MODELS));
        }

        @Override
        public void preRender(
                PoseStack poseStack,
                ModelBlockItem animatable,
                BakedGeoModel model,
                @Nullable MultiBufferSource bufferSource,
                @Nullable VertexConsumer buffer,
                boolean isReRender,
                float partialTick,
                int packedLight,
                int packedOverlay,
                int renderColor) {
            super.preRender(
                    poseStack,
                    animatable,
                    model,
                    bufferSource,
                    buffer,
                    isReRender,
                    partialTick,
                    packedLight,
                    packedOverlay,
                    renderColor);

            if (!isReRender) {
                poseStack.translate(0, -0.51f, 0);
            }
        }
    }
}
