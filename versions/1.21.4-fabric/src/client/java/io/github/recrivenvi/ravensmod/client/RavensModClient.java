package io.github.recrivenvi.ravensmod.client;

import io.github.recrivenvi.ravensmod.block.entity.AnimatedBlockEntity;
import io.github.recrivenvi.ravensmod.client.model.StaticGeoModel;
import io.github.recrivenvi.ravensmod.client.render.StaticBlockItemRenderer;
import io.github.recrivenvi.ravensmod.client.render.VerticalBlockEntityRenderer;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
import io.github.recrivenvi.ravensmod.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class RavensModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerHorizontal(ModContent.SHATTERED_THRONE, ModContent.SHATTERED_THRONE_ENTITY, "shattered_throne", "mirror_block");
        registerHorizontal(ModContent.ROUND_CORNER_ONE, ModContent.ROUND_CORNER_ONE_ENTITY, "round_corner_1", "white_block");
        registerHorizontal(ModContent.ROUND_CORNER_TWO, ModContent.ROUND_CORNER_TWO_ENTITY, "round_corner_2", "white_block");
        registerVertical(ModContent.FLAT_WALL, ModContent.FLAT_WALL_ENTITY, "ball", "white_block");
        registerHorizontal(ModContent.VERTICAL_WALL, ModContent.VERTICAL_WALL_ENTITY, "ball_2", "white_block");
    }

    private static <T extends AnimatedBlockEntity> void registerHorizontal(
            Block block, BlockEntityType<T> type, String model, String texture) {
        registerItemRenderer(block, model, texture);
        BlockEntityRenderers.register(type, context -> new GeoBlockRenderer<>(new StaticGeoModel<>(model, texture)));
    }

    private static <T extends AnimatedBlockEntity> void registerVertical(
            Block block, BlockEntityType<T> type, String model, String texture) {
        registerItemRenderer(block, model, texture);
        BlockEntityRenderers.register(type, context -> new VerticalBlockEntityRenderer<>(new StaticGeoModel<>(model, texture)));
    }

    private static void registerItemRenderer(Block block, String model, String texture) {
        AnimatedBlockItem item = (AnimatedBlockItem)block.asItem();

        item.renderProvider.setValue(new GeoRenderProvider() {
            private GeoItemRenderer<?> renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new StaticBlockItemRenderer(new StaticGeoModel<>(model, texture));
                }

                return this.renderer;
            }
        });
    }
}
