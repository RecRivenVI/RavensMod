package io.github.recrivenvi.ravensmodels.client;

import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.RavensModels;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(
        modid = RavensModels.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT)
public final class RavensModelsClient {
    private static final ModelGeometryLoader MODELS = new ModelGeometryLoader();

    private RavensModelsClient() {}

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, "mesh"), MODELS);
    }

    @SubscribeEvent
    public static void registerReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(MODELS);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModContent.CHAIR_SEAT.get(), NoopRenderer::new);
    }
}
