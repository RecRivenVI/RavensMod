package io.github.recrivenvi.ravensmodels.client;

import net.fabricmc.api.ClientModInitializer;

public final class RavensModelsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        SphereBakedModel.register();
        DodecahedronBakedModel.register();
        G3CornerBakedModel.register();
        ThroneBakedModel.register();
    }
}
