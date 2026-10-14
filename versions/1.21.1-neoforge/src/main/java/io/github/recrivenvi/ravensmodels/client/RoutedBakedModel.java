package io.github.recrivenvi.ravensmodels.client;

import io.github.recrivenvi.ravensmodels.block.DecorativeModelBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/** One terrain/instance decision for every custom model, independent of its asset format. */
public final class RoutedBakedModel extends BakedModelWrapper<BakedModel> {
    static volatile boolean instancingRegistered;
    private static final ModelProperty<Boolean> VISUAL_ANCHOR = new ModelProperty<>();

    public RoutedBakedModel(BakedModel model) {
        super(model);
    }

    public static boolean usingFlywheel() {
        return instancingRegistered && DecorativeInstanceVisual.active();
    }

    public static boolean hasAnchor(ModelData data) {
        return Boolean.TRUE.equals(data.get(VISUAL_ANCHOR));
    }

    @Override
    public ModelData getModelData(
            BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        ModelData result = this.originalModel.getModelData(level, pos, state, data);
        return level.getBlockEntity(pos) instanceof DecorativeModelBlockEntity
                ? result.derive().with(VISUAL_ANCHOR, true).build()
                : result;
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType layer) {
        return state != null && hasAnchor(data) && usingFlywheel()
                ? List.of()
                : this.originalModel.getQuads(state, side, random, data, layer);
    }
}
