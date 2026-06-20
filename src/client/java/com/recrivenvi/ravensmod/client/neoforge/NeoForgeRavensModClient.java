package com.recrivenvi.ravensmod.client.neoforge;

import com.recrivenvi.ravensmod.client.ClientInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

//? >=26.1 {
@EventBusSubscriber(modid = "ravensmod", value = Dist.CLIENT)
//?} else {
/*@EventBusSubscriber(modid = "ravensmod", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)*/
//?}
public class NeoForgeRavensModClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientInit::init);
    }
}
