package io.github.recrivenvi.ravensmodels.block;

import io.github.recrivenvi.ravensmodels.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/** Exists only while a player occupies a chair; it is never written to the world save. */
public final class ChairSeatEntity extends Entity {
    private BlockPos chairPos = BlockPos.ZERO;
    private UUID alignedPassenger;

    public ChairSeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
        setInvulnerable(true);
    }

    public static List<ChairSeatEntity> at(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(ChairSeatEntity.class, new AABB(pos).inflate(0.25),
                seat -> !seat.isRemoved() && seat.chairPos.equals(pos));
    }

    public static boolean sit(ServerLevel level, BlockPos pos, Direction facing, Player player) {
        ChairSeatEntity seat = new ChairSeatEntity(ModContent.CHAIR_SEAT, level);
        seat.chairPos = pos.immutable();
        seat.setPos(PlasticChairBlock.seatPosition(pos, facing));
        seat.setYRot(facing.toYRot());
        if (!level.addFreshEntity(seat)) {
            return false;
        }
        if (!player.startRiding(seat)) {
            seat.discard();
            return false;
        }
        seat.positionRider(player);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            BlockState state = server.getBlockState(this.chairPos);
            Entity rider = getFirstPassenger();
            if (rider == null || rider.isRemoved() || !rider.isAlive()
                    || rider.level() != level() || !state.is(ModContent.PLASTIC_CHAIR)) {
                ejectPassengers();
                discard();
                return;
            }
            Direction facing = state.getValue(PlasticChairBlock.FACING);
            setPos(PlasticChairBlock.seatPosition(this.chairPos, facing));
            setYRot(facing.toYRot());
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction move) {
        super.positionRider(passenger, move);
        if (!passenger.getUUID().equals(this.alignedPassenger)) {
            passenger.setYRot(getYRot());
            this.alignedPassenger = passenger.getUUID();
        }
        if (passenger instanceof LivingEntity living) {
            living.setYBodyRot(getYRot());
            float difference = Mth.wrapDegrees(passenger.getYRot() - getYRot());
            float correction = Mth.clamp(difference, -85.0f, 85.0f) - difference;
            passenger.yRotO += correction;
            passenger.setYRot(passenger.getYRot() + correction);
            living.setYHeadRot(passenger.getYRot());
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return passenger instanceof Player && !isVehicle();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Direction facing = Direction.fromYRot(getYRot());
        Direction[] directions = {facing, facing.getClockWise(), facing.getCounterClockWise(), facing.getOpposite()};
        for (int distance = 1; distance <= 2; distance++) {
            for (Direction direction : directions) {
                for (int up = 0; up <= 1; up++) {
                    Vec3 candidate = DismountHelper.findSafeDismountLocation(
                            passenger.getType(), level(), this.chairPos.relative(direction, distance).above(up), true);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }
        return Vec3.atBottomCenterOf(this.chairPos).add(0, PlasticChairBlock.MODEL_HEIGHT + 0.01, 0);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    // Minecraft checks the type's serialization flag before allowing passengers.
    // The type permits riding; these temporary instances still never enter saves.
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean save(CompoundTag tag) {
        return false;
    }

    @Override
    public boolean saveAsPassenger(CompoundTag tag) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
