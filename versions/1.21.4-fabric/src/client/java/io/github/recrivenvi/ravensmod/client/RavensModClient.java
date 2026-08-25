package io.github.recrivenvi.ravensmod.client;

import io.github.recrivenvi.ravensmod.client.ClientInit;
import net.fabricmc.api.ClientModInitializer;

public class RavensModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientInit.init();
    }
}
