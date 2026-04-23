package com.recrivenvi.ravensmod;

import com.recrivenvi.ravensmod.register.BlocksX;
import com.recrivenvi.ravensmod.register.ItemsX;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RavensMod implements ModInitializer {
    public static final String MOD_ID = "ravensmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BlocksX.initialize();
        RegisterBlockEntities.initialize();
        ItemsX.initialize();
        LOGGER.info("A BIG HELLO FROM RECTECH!!!");
    }
}