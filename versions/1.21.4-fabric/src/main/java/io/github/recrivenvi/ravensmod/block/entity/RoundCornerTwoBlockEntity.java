package io.github.recrivenvi.ravensmod.block.entity;

import io.github.recrivenvi.ravensmod.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class RoundCornerTwoBlockEntity extends AnimatedBlockEntity {
    public RoundCornerTwoBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLOCK_ENTITY_3, pos, state);
    }
}
