package io.github.recrivenvi.ravensmodels.client;

import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.ModelBlock;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;

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
    public static void routeModels(ModelEvent.ModifyBakingResult event) {
        var ids =
                ModContent.modelBlocks().stream()
                        .map(BuiltInRegistries.BLOCK::getKey)
                        .collect(java.util.stream.Collectors.toSet());
        event.getModels()
                .replaceAll(
                        (location, model) ->
                                ids.contains(location.id()) && !(model instanceof RoutedBakedModel)
                                        ? new RoutedBakedModel(model)
                                        : model);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModContent.CHAIR_SEAT.get(), NoopRenderer::new);
        // Registers the native block-entity lifecycle; geometry is drawn by Flywheel or terrain.
        event.registerBlockEntityRenderer(
                ModContent.CHAIR_MODEL.get(),
                context -> (blockEntity, partialTick, poseStack, buffers, light, overlay) -> {});
        event.registerBlockEntityRenderer(
                ModContent.DAVID_BUST_MODEL.get(),
                context -> (blockEntity, partialTick, poseStack, buffers, light, overlay) -> {});
        event.registerBlockEntityRenderer(
                ModContent.MODEL_BLOCK.get(),
                context -> (blockEntity, partialTick, poseStack, buffers, light, overlay) -> {});
    }

    @SubscribeEvent
    public static void setupInstances(FMLClientSetupEvent event) {
        if (ModList.get().isLoaded("flywheel")) {
            event.enqueueWork(DecorativeInstanceVisual::register);
        }
        NeoForge.EVENT_BUS.addListener(RavensModelsClient::initializeAnchors);
    }

    private static void initializeAnchors(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk) || !chunk.getLevel().isClientSide())
            return;
        // Existing decorative blocks may predate their data-free render anchors.
        var sections = chunk.getSections();
        for (int index = 0; index < sections.length; index++) {
            var section = sections[index];
            if (!section.maybeHas(state -> state.getBlock() instanceof ModelBlock)) continue;
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        var state = section.getBlockState(x, y, z);
                        if (state.getBlock() instanceof ModelBlock) {
                            chunk.getBlockEntity(
                                    new BlockPos(
                                            chunk.getPos().getMinBlockX() + x,
                                            baseY + y,
                                            chunk.getPos().getMinBlockZ() + z),
                                    LevelChunk.EntityCreationType.IMMEDIATE);
                        }
                    }
                }
            }
        }
    }
}
