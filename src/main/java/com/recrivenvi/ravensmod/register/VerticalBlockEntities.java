package com.recrivenvi.ravensmod.register;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VerticalBlockEntities<E extends BlockEntity> extends BaseEntityBlock {
    public static final Property<Direction> FACING = BlockStateProperties.VERTICAL_DIRECTION;

    private final BlockEntities.EntityFactory<E> entityFactory;

    public VerticalBlockEntities(Properties settings, BlockEntities.EntityFactory<E> entityFactory) {
        super(settings);
        this.entityFactory = entityFactory;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return RecordCodecBuilder.mapCodec(instance ->
                instance.group(propertiesCodec()).apply(instance, (props) -> new VerticalBlockEntities<>(props, this.entityFactory))
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face = ctx.getClickedFace();
        if (face != Direction.UP && face != Direction.DOWN) {
            face = Direction.DOWN;
        }

        return this.defaultBlockState().setValue(FACING, face);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entityFactory.create(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
