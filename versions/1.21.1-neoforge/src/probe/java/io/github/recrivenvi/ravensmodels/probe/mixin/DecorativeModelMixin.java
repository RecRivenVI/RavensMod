package io.github.recrivenvi.ravensmodels.probe.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.recrivenvi.ravensmodels.client.RoutedBakedModel;
import io.github.recrivenvi.ravensmodels.probe.ChunkRebuildProbe;
import io.github.recrivenvi.ravensmodels.probe.FallbackRenderProbe;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RoutedBakedModel.class, remap = false)
public abstract class DecorativeModelMixin {
    @ModifyReturnValue(
            method =
                    "getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;",
            at = @At("RETURN"),
            remap = false)
    private List<BakedQuad> returned(
            List<BakedQuad> original,
            BlockState state,
            Direction side,
            RandomSource random,
            ModelData data,
            RenderType layer) {
        List<BakedQuad> result =
                state != null && RoutedBakedModel.hasAnchor(data) && FallbackRenderProbe.useBer()
                        ? List.of()
                        : original;
        if (state != null) {
            ChunkRebuildProbe.quads(state, side, result.size());
        }
        FallbackRenderProbe.quads(result.size());
        return result;
    }
}
