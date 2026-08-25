package io.github.recrivenvi.ravensmod.block.entity;

import io.github.recrivenvi.ravensmod.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class FlatWallBlockEntity extends AnimatedBlockEntity {
    public FlatWallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLOCK_ENTITY_4, pos, state);
    }
}
