package io.github.recrivenvi.ravensmodels;

import net.fabricmc.api.ModInitializer;

public final class RavensModels implements ModInitializer {
    public static final String MOD_ID = "ravensmodels";

    @Override
    public void onInitialize() {
        ModContent.initialize();
    }
}
