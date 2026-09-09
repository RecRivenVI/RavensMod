package io.github.recrivenvi.ravensmodels.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PlasticChairBlock extends HorizontalFacingBlock {
    public static final float MODEL_SCALE = 1.6f;
    public static final double SEAT_HEIGHT = 0.452 * MODEL_SCALE;
    public static final double MODEL_HEIGHT = 0.8798411972820759 * MODEL_SCALE;
    private static final MapCodec<PlasticChairBlock> CODEC = simpleCodec(PlasticChairBlock::new);
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            modelBox(0.17, 0, 0.20, 0.28, 0.43, 0.33),
            modelBox(0.72, 0, 0.20, 0.83, 0.43, 0.33),
            modelBox(0.23, 0, 0.68, 0.34, 0.43, 0.84),
            modelBox(0.66, 0, 0.68, 0.77, 0.43, 0.84),
            modelBox(0.18, 0.36, 0.20, 0.82, 0.452, 0.74),
            modelBox(0.27, 0.43, 0.70, 0.73, 0.88, 0.84),
            modelBox(0.17, 0.43, 0.22, 0.26, 0.64, 0.73),
            modelBox(0.74, 0.43, 0.22, 0.83, 0.64, 0.73));

    private final ImmutableMap<BlockState, VoxelShape> shapes;

    public PlasticChairBlock(Properties properties) {
        super(properties);
        this.shapes = getShapeForEachState(state -> rotateShape(NORTH_SHAPE, state.getValue(FACING)));
    }

    @Override
    protected MapCodec<PlasticChairBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.shapes.get(state);
    }

    public static Vec3 seatPosition(BlockPos pos, Direction facing) {
        double backOffset = 0.07 * MODEL_SCALE;
        return Vec3.atBottomCenterOf(pos).add(
                -facing.getStepX() * backOffset, SEAT_HEIGHT, -facing.getStepZ() * backOffset);
    }

    private static VoxelShape modelBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Shapes.box(
                (minX - 0.5) * MODEL_SCALE + 0.5, minY * MODEL_SCALE, (minZ - 0.5) * MODEL_SCALE + 0.5,
                (maxX - 0.5) * MODEL_SCALE + 0.5, maxY * MODEL_SCALE, (maxZ - 0.5) * MODEL_SCALE + 0.5);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isSecondaryUseActive() || player.isSpectator() || !player.isAlive() || player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        for (ChairSeatEntity seat : ChairSeatEntity.at(server, pos)) {
            if (seat.isVehicle()) {
                player.displayClientMessage(Component.translatable("message.ravensmodels.chair_occupied"), true);
                return InteractionResult.CONSUME;
            }
            seat.discard();
        }
        return ChairSeatEntity.sit(server, pos, state.getValue(FACING), player)
                ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            for (ChairSeatEntity seat : ChairSeatEntity.at(server, pos)) {
                seat.ejectPassengers();
                seat.discard();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
