package io.github.recrivenvi.ravensmodels.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AceOfSpadesBlock extends HorizontalFacingBlock {
    public static final EnumProperty<AttachFace> FACE = EnumProperty.create(
            "face", AttachFace.class, AttachFace.FLOOR, AttachFace.WALL);
    public static final float DISPLAY_LENGTH = 2.0f;
    private static final MapCodec<AceOfSpadesBlock> CODEC = simpleCodec(AceOfSpadesBlock::new);
    // Selection/collision belongs to one cell; only the displayed geometry overhangs it.
    private static final VoxelShape FLOOR_SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    private static final VoxelShape WALL_SHAPE = Block.box(0, 0, 12, 16, 16, 16);
    private final ImmutableMap<BlockState, VoxelShape> shapes;

    public AceOfSpadesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACE, AttachFace.FLOOR));
        this.shapes = getShapeForEachState(state -> rotateShape(
                state.getValue(FACE) == AttachFace.FLOOR ? FLOOR_SHAPE : WALL_SHAPE, state.getValue(FACING)));
    }

    @Override
    protected MapCodec<AceOfSpadesBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        BlockState state = super.getStateForPlacement(context);
        // The clicked face selects presentation only, not an attachment requirement.
        return clickedFace.getAxis().isVertical()
                ? state.setValue(FACE, AttachFace.FLOOR)
                : state.setValue(FACE, AttachFace.WALL).setValue(FACING, clickedFace);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACE);
    }
}
