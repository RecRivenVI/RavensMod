package io.github.recrivenvi.ravensmodels;

import io.github.recrivenvi.ravensmodels.block.ChairSeatEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@Mod(RavensModels.MOD_ID)
public final class RavensModels {
    public static final String MOD_ID = "${mod_id}";

    public RavensModels(IEventBus bus) {
        ModContent.initialize(bus);
        NeoForge.EVENT_BUS.addListener(RavensModels::onLogout);
        NeoForge.EVENT_BUS.addListener(RavensModels::onServerStopping);
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().getVehicle() instanceof ChairSeatEntity) {
            event.getEntity().stopRiding();
        }
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            if (player.getVehicle() instanceof ChairSeatEntity) {
                player.stopRiding();
            }
        }
    }
}
