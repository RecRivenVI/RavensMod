package io.github.recrivenvi.ravensmodels.probe.mixin;

import io.github.recrivenvi.ravensmodels.probe.ChunkRebuildProbe;
import io.github.recrivenvi.ravensmodels.probe.FallbackRenderProbe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
        targets =
                "net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask",
        remap = false)
public abstract class ChunkTaskMixin {
    @Inject(
            method =
                    "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At("HEAD"),
            remap = false)
    private void begin(CallbackInfoReturnable<Object> callback) {
        ChunkRebuildProbe.begin(this);
        FallbackRenderProbe.begin(this);
    }

    @Inject(
            method =
                    "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
            at = @At("RETURN"),
            remap = false)
    private void end(CallbackInfoReturnable<Object> callback) {
        ChunkRebuildProbe.end(callback.getReturnValue() != null);
        FallbackRenderProbe.end(callback.getReturnValue() != null);
    }
}
