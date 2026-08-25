package io.github.recrivenvi.ravensmod.block.entity;

import io.github.recrivenvi.ravensmod.block.entity.AnimatedBlockEntity;
import io.github.recrivenvi.ravensmod.registry.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class ShatteredThroneBlockEntity extends AnimatedBlockEntity {
    public ShatteredThroneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.BLOCK_ENTITY_1, pos, state);
    }
}
