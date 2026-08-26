package io.github.recrivenvi.ravensmodels.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class ModelBlock extends Block implements EntityBlock {
    private static final MapCodec<ModelBlock> CODEC = simpleCodec(ModelBlock::new);

    public ModelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<ModelBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModelBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
