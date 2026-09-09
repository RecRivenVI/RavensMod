package io.github.recrivenvi.ravensmodels;

import net.fabricmc.api.ModInitializer;
import io.github.recrivenvi.ravensmodels.block.ChairSeatEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class RavensModels implements ModInitializer {
    public static final String MOD_ID = "ravensmodels";

    @Override
    public void onInitialize() {
        ModContent.initialize();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler.player.getVehicle() instanceof ChairSeatEntity) {
                handler.player.stopRiding();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                if (player.getVehicle() instanceof ChairSeatEntity) {
                    player.stopRiding();
                }
            }
        });
    }
}
