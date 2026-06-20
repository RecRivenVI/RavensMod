package com.recrivenvi.ravensmod.client.fabric;

import com.recrivenvi.ravensmod.client.ClientInit;
import net.fabricmc.api.ClientModInitializer;

public class FabricRavensModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientInit.init();
    }
}
