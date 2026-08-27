package io.github.recrivenvi.ravensmodels.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class G3StairBlock extends StairBlock {
    private static final MapCodec<G3StairBlock> CODEC = simpleCodec(G3StairBlock::new);

    private static final VoxelShape BOTTOM_PLATE = box(0, 0, 0, 16, 1, 16);
    private static final VoxelShape TOP_PLATE = box(0, 15, 0, 16, 16, 16);
    private static final VoxelShape NORTH_PLATE = box(0, 0, 0, 16, 16, 1);
    private static final VoxelShape SOUTH_PLATE = box(0, 0, 15, 16, 16, 16);
    private static final VoxelShape WEST_PLATE = box(0, 0, 0, 1, 16, 16);
    private static final VoxelShape EAST_PLATE = box(15, 0, 0, 16, 16, 16);

    private final ImmutableMap<BlockState, VoxelShape> shapes;

    public G3StairBlock(Properties properties) {
        super(Blocks.STONE.defaultBlockState(), properties);
        this.shapes = getShapeForEachState(G3StairBlock::shapeForState);
    }

    @Override
    public MapCodec<G3StairBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(new BlockPlaceContext(context) {
            @Override
            public Direction getHorizontalDirection() {
                return context.getHorizontalDirection().getOpposite();
            }
        });
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

    private static VoxelShape shapeForState(BlockState state) {
        VoxelShape horizontal = state.getValue(HALF) == Half.TOP ? TOP_PLATE : BOTTOM_PLATE;
        Direction facing = state.getValue(FACING);
        Direction primarySide = facing.getOpposite();
        StairsShape shape = state.getValue(SHAPE);

        return switch (shape) {
            case INNER_LEFT -> Shapes.or(
                    horizontal,
                    cornerPost(primarySide, facing.getClockWise()));
            case INNER_RIGHT -> Shapes.or(
                    horizontal,
                    cornerPost(primarySide, facing.getCounterClockWise()));
            case OUTER_LEFT -> Shapes.or(
                    horizontal,
                    sidePlate(primarySide),
                    sidePlate(facing.getClockWise()));
            case OUTER_RIGHT -> Shapes.or(
                    horizontal,
                    sidePlate(primarySide),
                    sidePlate(facing.getCounterClockWise()));
            default -> Shapes.or(horizontal, sidePlate(primarySide));
        };
    }

    private static VoxelShape sidePlate(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH_PLATE;
            case SOUTH -> SOUTH_PLATE;
            case WEST -> WEST_PLATE;
            case EAST -> EAST_PLATE;
            default -> throw new IllegalArgumentException("G3 side must be horizontal: " + direction);
        };
    }

    private static VoxelShape cornerPost(Direction first, Direction second) {
        int minX = first == Direction.WEST || second == Direction.WEST ? 0 : 15;
        int minZ = first == Direction.NORTH || second == Direction.NORTH ? 0 : 15;
        return box(minX, 0, minZ, minX + 1, 16, minZ + 1);
    }
}
