package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntity2Register extends BlockEntitiesRegister {
    public BlockEntity2Register(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.BLOCK_ENTITY_2, pos, state);
    }
}
