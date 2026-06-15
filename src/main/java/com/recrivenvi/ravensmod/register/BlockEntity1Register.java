package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesRegister;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntity1Register extends CompatBlockEntitiesRegister {
    public BlockEntity1Register(BlockPos pos, BlockState state) {
        super(RegisterBlockEntities.BLOCK_ENTITY_1, pos, state);
    }
}
