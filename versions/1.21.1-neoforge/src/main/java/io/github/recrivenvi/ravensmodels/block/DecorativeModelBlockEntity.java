package io.github.recrivenvi.ravensmodels.block;

import io.github.recrivenvi.ravensmodels.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Lifecycle anchor for the optional client-side persistent mesh; no data or ticking. */
public final class DecorativeModelBlockEntity extends BlockEntity {
    public DecorativeModelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static DecorativeModelBlockEntity create(BlockPos pos, BlockState state) {
        BlockEntityType<?> type =
                state.is(ModContent.PLASTIC_CHAIR.get())
                        ? ModContent.CHAIR_MODEL.get()
                        : state.is(ModContent.DAVID_BUST.get())
                                ? ModContent.DAVID_BUST_MODEL.get()
                                : ModContent.MODEL_BLOCK.get();
        return new DecorativeModelBlockEntity(type, pos, state);
    }
}
