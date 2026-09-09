package io.github.recrivenvi.ravensmodels.client;

import net.fabricmc.api.ClientModInitializer;
import io.github.recrivenvi.ravensmodels.ModContent;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.NoopRenderer;

public final class RavensModelsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SphereBakedModel.register();
        DodecahedronBakedModel.register();
        G3CornerBakedModel.register();
        ThroneBakedModel.register();
        DecorativeMeshBakedModel.register();
        AceOfSpadesBakedModel.register();
        EntityRendererRegistry.register(ModContent.CHAIR_SEAT, NoopRenderer::new);
    }
}
