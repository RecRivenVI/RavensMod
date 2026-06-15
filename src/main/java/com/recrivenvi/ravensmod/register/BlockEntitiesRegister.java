package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEntitiesRegister extends CompatBlockEntitiesRegister {

    public BlockEntitiesRegister(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
