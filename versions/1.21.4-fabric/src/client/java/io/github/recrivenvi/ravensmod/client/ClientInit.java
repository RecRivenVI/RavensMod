package io.github.recrivenvi.ravensmod.client;

import io.github.recrivenvi.ravensmod.client.model.RavensGeoModel;
import io.github.recrivenvi.ravensmod.client.render.HorizontalBlockEntityRenderer;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
import io.github.recrivenvi.ravensmod.client.render.AnimatedBlockItemRenderProvider;
import io.github.recrivenvi.ravensmod.client.render.VerticalBlockEntityRenderer;
import io.github.recrivenvi.ravensmod.registry.ModBlocks;
import io.github.recrivenvi.ravensmod.registry.ModBlockEntityTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientInit {
    public static void init() {
        AnimatedBlockItem.registerRenderProvider(ModBlocks.BLOCK_ENTITY_1, new AnimatedBlockItemRenderProvider("shattered_throne", "mirror_block", null));
        AnimatedBlockItem.registerRenderProvider(ModBlocks.BLOCK_ENTITY_2, new AnimatedBlockItemRenderProvider("round_corner_1", "white_block", null));
        AnimatedBlockItem.registerRenderProvider(ModBlocks.BLOCK_ENTITY_3, new AnimatedBlockItemRenderProvider("round_corner_2", "white_block", null));
        AnimatedBlockItem.registerRenderProvider(ModBlocks.BLOCK_ENTITY_4, new AnimatedBlockItemRenderProvider("ball", "white_block", null));
        AnimatedBlockItem.registerRenderProvider(ModBlocks.BLOCK_ENTITY_5, new AnimatedBlockItemRenderProvider("ball_2", "white_block", null));

        BlockEntityRenderers.register(ModBlockEntityTypes.BLOCK_ENTITY_1, ctx -> new HorizontalBlockEntityRenderer<>(new RavensGeoModel<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(ModBlockEntityTypes.BLOCK_ENTITY_2, ctx -> new HorizontalBlockEntityRenderer<>(new RavensGeoModel<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(ModBlockEntityTypes.BLOCK_ENTITY_3, ctx -> new HorizontalBlockEntityRenderer<>(new RavensGeoModel<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(ModBlockEntityTypes.BLOCK_ENTITY_4, ctx -> new VerticalBlockEntityRenderer<>(new RavensGeoModel<>("ball", "white_block", null)));
        BlockEntityRenderers.register(ModBlockEntityTypes.BLOCK_ENTITY_5, ctx -> new HorizontalBlockEntityRenderer<>(new RavensGeoModel<>("ball_2", "white_block", null)));
    }
}
