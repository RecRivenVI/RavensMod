package io.github.recrivenvi.ravensmod.block.entity;

import io.github.recrivenvi.ravensmod.registry.ModContent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AnimatedBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected AnimatedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static final class ShatteredThrone extends AnimatedBlockEntity {
        public ShatteredThrone(BlockPos pos, BlockState state) {
            super(ModContent.SHATTERED_THRONE_ENTITY, pos, state);
        }
    }

    public static final class RoundCornerOne extends AnimatedBlockEntity {
        public RoundCornerOne(BlockPos pos, BlockState state) {
            super(ModContent.ROUND_CORNER_ONE_ENTITY, pos, state);
        }
    }

    public static final class RoundCornerTwo extends AnimatedBlockEntity {
        public RoundCornerTwo(BlockPos pos, BlockState state) {
            super(ModContent.ROUND_CORNER_TWO_ENTITY, pos, state);
        }
    }

    public static final class FlatWall extends AnimatedBlockEntity {
        public FlatWall(BlockPos pos, BlockState state) {
            super(ModContent.FLAT_WALL_ENTITY, pos, state);
        }
    }

    public static final class VerticalWall extends AnimatedBlockEntity {
        public VerticalWall(BlockPos pos, BlockState state) {
            super(ModContent.VERTICAL_WALL_ENTITY, pos, state);
        }
    }
}
