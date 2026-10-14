package io.github.recrivenvi.ravensmodels.client;

import dev.engine_room.flywheel.api.backend.BackendManager;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.baked.BlockModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.DecorativeModelBlockEntity;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Immutable state meshes shared by every world position; state geometry remains authoritative. */
public final class DecorativeInstanceVisual
        extends AbstractBlockEntityVisual<DecorativeModelBlockEntity> implements ShaderLightVisual {
    private static final Logger LOGGER = LoggerFactory.getLogger(RavensModels.MOD_ID);
    private static final RendererReloadCache<BlockState, Model> MODELS =
            new RendererReloadCache<>(DecorativeInstanceVisual::bake);
    private final TransformedInstance instance;

    public static void register() {
        SimpleBlockEntityVisualizer.builder(ModContent.CHAIR_MODEL.get())
                .factory(DecorativeInstanceVisual::new)
                .apply();
        SimpleBlockEntityVisualizer.builder(ModContent.DAVID_BUST_MODEL.get())
                .factory(DecorativeInstanceVisual::new)
                .apply();
        SimpleBlockEntityVisualizer.builder(ModContent.MODEL_BLOCK.get())
                .factory(DecorativeInstanceVisual::new)
                .apply();
        RoutedBakedModel.instancingRegistered = true;
    }

    static boolean active() {
        return VisualizationManager.supportsVisualization(Minecraft.getInstance().level);
    }

    public DecorativeInstanceVisual(
            VisualizationContext context,
            DecorativeModelBlockEntity blockEntity,
            float partialTick) {
        super(context, blockEntity, partialTick);
        BlockState meshState =
                this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                        ? this.blockState.setValue(BlockStateProperties.WATERLOGGED, false)
                        : this.blockState;
        Model model = MODELS.get(meshState);
        this.instance =
                instancerProvider().instancer(InstanceTypes.TRANSFORMED, model).createInstance();
        this.instance.translate(
                this.visualPos.getX(), this.visualPos.getY(), this.visualPos.getZ());
        updateLight(partialTick);
        this.instance.setChanged();
    }

    private static Model bake(BlockState state) {
        var baked = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        int expectedVertices = baked.getQuads(state, null, RandomSource.create()).size() * 4;
        for (Direction side : Direction.values()) {
            expectedVertices += baked.getQuads(state, side, RandomSource.create()).size() * 4;
        }
        Model model =
                new BlockModelBuilder(
                                SinglePosVirtualBlockGetter.createFullDark().blockState(state),
                                List.of(BlockPos.ZERO))
                        .materialFunc(
                                (layer, shaded, ambientOcclusion) ->
                                        SimpleMaterial.builderOf(
                                                        ModelUtil.getMaterial(
                                                                layer, shaded, ambientOcclusion))
                                                .light(LightShaders.SMOOTH)
                                                .build())
                        .build();
        int actualVertices =
                ModelUtil.computeTotalVertexCount(
                        model.meshes().stream().map(Model.ConfiguredMesh::mesh).toList());
        if (actualVertices != expectedVertices) {
            throw new IllegalStateException(
                    "Persistent decorative mesh lost vertices: "
                            + actualVertices
                            + " != "
                            + expectedVertices);
        }
        LOGGER.info(
                "Persistent mesh {}: {} vertices, {} quads, shared GPU geometry; backend {}",
                state.getBlock(),
                actualVertices,
                actualVertices / 4,
                BackendManager.currentBackend());
        return model;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(this.instance);
    }

    @Override
    public void setSectionCollector(SectionCollector collector) {
        LongOpenHashSet sections = new LongOpenHashSet();
        for (int x = SectionPos.blockToSectionCoord(this.pos.getX() - 1);
                x <= SectionPos.blockToSectionCoord(this.pos.getX() + 2);
                x++) {
            for (int y = SectionPos.blockToSectionCoord(this.pos.getY() - 1);
                    y <= SectionPos.blockToSectionCoord(this.pos.getY() + 2);
                    y++) {
                for (int z = SectionPos.blockToSectionCoord(this.pos.getZ() - 1);
                        z <= SectionPos.blockToSectionCoord(this.pos.getZ() + 2);
                        z++) {
                    sections.add(SectionPos.asLong(x, y, z));
                }
            }
        }
        collector.sections(sections);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(this.instance);
    }

    @Override
    protected void _delete() {
        this.instance.delete();
    }
}
