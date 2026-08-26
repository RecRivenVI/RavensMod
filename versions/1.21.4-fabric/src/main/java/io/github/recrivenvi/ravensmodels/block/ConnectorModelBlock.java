package io.github.recrivenvi.ravensmodels.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public final class ConnectorModelBlock extends DirectionalBlock implements EntityBlock {
    private static final MapCodec<ConnectorModelBlock> CODEC = simpleCodec(ConnectorModelBlock::new);

    private static final VoxelShape NORTH_SHAPE = box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape SOUTH_SHAPE = box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape EAST_SHAPE = box(15, 0, 0, 16, 16, 16);
    private static final VoxelShape WEST_SHAPE = box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape UP_SHAPE = box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape DOWN_SHAPE = box(0, 0, 0, 16, 1, 16);

    private final ImmutableMap<BlockState, VoxelShape> shapes;

    public ConnectorModelBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.shapes = getShapeForEachState(ConnectorModelBlock::shapeForState);
    }

    @Override
    protected @NotNull MapCodec<ConnectorModelBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ModelBlockEntity(pos, state);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    private static VoxelShape shapeForState(BlockState state) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> EAST_SHAPE;
            case EAST -> WEST_SHAPE;
            case UP -> DOWN_SHAPE;
            case DOWN -> UP_SHAPE;
            default -> NORTH_SHAPE;
        };
    }
}
