package com.recrivenvi.ravensmod.client;

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesModels;
import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesRenderer;
import com.recrivenvi.ravensmod.compat.CompatBlockItem;
import com.recrivenvi.ravensmod.compat.CompatBlockItemRenderProvider;
import com.recrivenvi.ravensmod.compat.CompatVerticalBlockEntitiesRenderer;
import com.recrivenvi.ravensmod.register.BlocksX;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ClientInit {
    public static void init() {
        //? neoforge {
        /*CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_1.get(), new CompatBlockItemRenderProvider("shattered_throne", "mirror_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_2.get(), new CompatBlockItemRenderProvider("round_corner_1", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_3.get(), new CompatBlockItemRenderProvider("round_corner_2", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_4.get(), new CompatBlockItemRenderProvider("ball", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_5.get(), new CompatBlockItemRenderProvider("ball_2", "white_block", null));*/
        //?} else {
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_1, new CompatBlockItemRenderProvider("shattered_throne", "mirror_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_2, new CompatBlockItemRenderProvider("round_corner_1", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_3, new CompatBlockItemRenderProvider("round_corner_2", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_4, new CompatBlockItemRenderProvider("ball", "white_block", null));
        CompatBlockItem.registerRenderProvider(BlocksX.BLOCK_ENTITY_5, new CompatBlockItemRenderProvider("ball_2", "white_block", null));
        //?}

        //? >=26.1 {
        //? neoforge {
        /*BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_1.get(), ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_2.get(), ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_3.get(), ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_4.get(), ctx -> new CompatVerticalBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_5.get(), ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball_2", "white_block", null)));*/
        //?} else {
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_1, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_2, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_3, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_4, ctx -> new CompatVerticalBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_5, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball_2", "white_block", null)));
        //?}
        //?} else {
        /*//? neoforge {
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_1.get(), ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_2.get(), ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_3.get(), ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_4.get(), ctx -> new CompatVerticalBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("ball", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_5.get(), ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("ball_2", "white_block", null)));
        //?} else {
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_1, ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_2, ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_3, ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_4, ctx -> new CompatVerticalBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("ball", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_5, ctx -> new CompatBlockEntitiesRenderer<>(new CompatBlockEntitiesModels<>("ball_2", "white_block", null)));
        //?}*/
        //?}
    }
}
