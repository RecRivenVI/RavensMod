package com.recrivenvi.ravensmod.client;

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesModels;
import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesRenderer;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class RavensModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_1, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("shattered_throne", "mirror_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_2, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_1", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_3, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("round_corner_2", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_4, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball", "white_block", null)));
        BlockEntityRenderers.register(RegisterBlockEntities.BLOCK_ENTITY_5, ctx -> new CompatBlockEntitiesRenderer<>(ctx, new CompatBlockEntitiesModels<>("ball_2", "white_block", null)));
    }
}
