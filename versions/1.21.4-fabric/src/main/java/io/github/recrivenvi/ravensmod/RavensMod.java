package io.github.recrivenvi.ravensmod;

import io.github.recrivenvi.ravensmod.registry.ModContent;
import net.fabricmc.api.ModInitializer;

public final class RavensMod implements ModInitializer {
    public static final String MOD_ID = "ravensmod";

    @Override
    public void onInitialize() {
        ModContent.initialize();
    }
}
