package io.github.recrivenvi.ravensmodels.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Native lifecycle shared by non-cubic model blocks; geometry and interactions stay in their
 * owners.
 */
public interface ModelBlock extends EntityBlock {
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return DecorativeModelBlockEntity.create(pos, state);
    }
}
