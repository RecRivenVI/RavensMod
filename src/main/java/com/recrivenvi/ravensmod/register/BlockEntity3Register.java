package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntity3Register extends BlockEntitiesRegister {
    public BlockEntity3Register(BlockPos pos, BlockState state) {
        //? neoforge {
        /*super(RegisterBlockEntities.BLOCK_ENTITY_3.get(), pos, state);*/
        //?} else {
        super(RegisterBlockEntities.BLOCK_ENTITY_3, pos, state);
        //?}
    }
}
