package io.github.recrivenvi.ravensmodels.probe;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendManager;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.ModelBlock;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ClientCommandHandler;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = RavensModels.MOD_ID + "_probe", dist = Dist.CLIENT)
public final class ChunkRebuildProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(RavensModels.MOD_ID + "_probe");
    private static final ThreadLocal<Measurement> CURRENT = new ThreadLocal<>();
    private static final ConcurrentLinkedQueue<Measurement> COMPLETED =
            new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();
    private static SectionPos section;
    private final List<Measurement> samples = new ArrayList<>();
    private final Properties settings = new Properties();
    private long started = System.nanoTime();
    private boolean opening;
    private boolean prepared;
    private volatile boolean fixtureReady;
    private boolean waiting;
    private boolean done;
    private int settle;
    private int phase;
    private int completed;
    private int requestTicks;
    private long expectedChair;
    private long expectedBust;
    private long expectedTotal;
    private int expectedModels;
    private int selectedBackend = -1;
    private final String[] backends = new String[2];
    private boolean measured;
    private boolean stateUpdateRecreated;
    private BlockPos changedPos;
    private BlockState changedState;
    private Object previousVisual;
    private int stateUpdateTicks;

    public ChunkRebuildProbe(IEventBus bus) throws Exception {
        if (System.getProperty("validation.name", "").equals("performance-render_fallback")) {
            new FallbackRenderProbe(bus);
            return;
        }
        try (var reader =
                Files.newBufferedReader(
                        Path.of(System.getProperty("validation.task"), "scene.properties"))) {
            this.settings.load(reader);
        }
        section = SectionPos.of(number("section_x"), number("section_y"), number("section_z"));
        NeoForge.EVENT_BUS.addListener(this::tick);
    }

    private int number(String key) {
        return Integer.parseInt(this.settings.getProperty(key));
    }

    public static void begin(Object task) {
        if (!REQUESTED.get()) return;
        try {
            Field field = task.getClass().getDeclaredField("renderContext");
            field.setAccessible(true);
            Object context = field.get(task);
            if (section.equals(context.getClass().getMethod("getOrigin").invoke(context))
                    && REQUESTED.compareAndSet(true, false)) {
                CURRENT.set(new Measurement(System.nanoTime()));
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void quads(BlockState state, net.minecraft.core.Direction side, int count) {
        Measurement m = CURRENT.get();
        if (m == null) return;
        m.total += count;
        if (side != null) return;
        m.modelCalls++;
        if (state.is(ModContent.PLASTIC_CHAIR.get())) {
            m.chair += count;
            m.chairCalls++;
        }
        if (state.is(ModContent.DAVID_BUST.get())) {
            m.bust += count;
            m.bustCalls++;
        }
    }

    public static void end(boolean success) {
        Measurement m = CURRENT.get();
        if (m == null) return;
        m.nanos = System.nanoTime() - m.nanos;
        m.success = success;
        CURRENT.remove();
        COMPLETED.add(m);
    }

    private void tick(ClientTickEvent.Post event) {
        if (this.done) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (System.nanoTime() - this.started > 240_000_000_000L)
                throw new IllegalStateException("Benchmark timeout");
            if (!this.opening && mc.screen instanceof TitleScreen) {
                this.opening = true;
                mc.options.renderDistance().set(4);
                mc.options.pauseOnLostFocus = false;
                var flows = mc.createWorldOpenFlows();
                if (Files.isDirectory(
                        mc.gameDirectory.toPath().resolve("saves/rebuild_benchmark"))) {
                    flows.openWorld("rebuild_benchmark", () -> {});
                } else {
                    flows.createFreshLevel(
                            "rebuild_benchmark",
                            new LevelSettings(
                                    "Rebuild Benchmark",
                                    GameType.CREATIVE,
                                    false,
                                    Difficulty.PEACEFUL,
                                    true,
                                    new GameRules(),
                                    WorldDataConfiguration.DEFAULT),
                            new WorldOptions(12345L, false, false),
                            registries ->
                                    registries
                                            .registryOrThrow(Registries.WORLD_PRESET)
                                            .getHolderOrThrow(WorldPresets.FLAT)
                                            .value()
                                            .createWorldDimensions(),
                            mc.screen);
                }
            }
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            if (this.measured) {
                verifyStateUpdate(mc);
                return;
            }
            if (!this.prepared) {
                this.prepared = true;
                expectedChair = quadsPerModel(mc, ModContent.PLASTIC_CHAIR.get());
                expectedBust =
                        (long) number("busts") * quadsPerModel(mc, ModContent.DAVID_BUST.get());
                mc.getSingleplayerServer()
                        .execute(
                                () -> {
                                    var level = mc.getSingleplayerServer().overworld();
                                    int x = section.minBlockX(),
                                            y = section.minBlockY() + 4,
                                            z = section.minBlockZ();
                                    for (int i = 0; i < number("busts"); i++) {
                                        level.setBlockAndUpdate(
                                                new BlockPos(
                                                        x + 1 + (i % 6) * 2,
                                                        y,
                                                        z + 1 + (i / 6) * 3),
                                                ModContent.DAVID_BUST.get().defaultBlockState());
                                    }
                                    level.setBlockAndUpdate(
                                            new BlockPos(x + 12, y, z + 4),
                                            ModContent.PLASTIC_CHAIR.get().defaultBlockState());
                                    level.setBlockAndUpdate(
                                            new BlockPos(x + 14, y, z + 4),
                                            ModContent.SHATTERED_THRONE.get().defaultBlockState());
                                    int extra = 0;
                                    for (Block block : ModContent.modelBlocks()) {
                                        if (block == ModContent.DAVID_BUST.get()
                                                || block == ModContent.PLASTIC_CHAIR.get()
                                                || block == ModContent.SHATTERED_THRONE.get())
                                            continue;
                                        level.setBlockAndUpdate(
                                                new BlockPos(x + 1 + extra++ * 2, y + 4, z + 11),
                                                block.defaultBlockState());
                                    }
                                    var player =
                                            mc.getSingleplayerServer()
                                                    .getPlayerList()
                                                    .getPlayer(mc.player.getUUID());
                                    player.teleportTo(x + 8, y + 3, z + 18);
                                    player.setYRot(180);
                                    player.setXRot(20);
                                    this.fixtureReady = true;
                                });
                return;
            }
            if (!this.fixtureReady) return;
            if (this.selectedBackend != this.phase) {
                ClientCommandHandler.getDispatcher()
                        .execute(
                                "flywheel backend "
                                        + (this.phase == 0 ? "flywheel:off" : "DEFAULT"),
                                ClientCommandHandler.getSource());
                if (BackendManager.isBackendOn() != (this.phase == 1))
                    throw new IllegalStateException("Flywheel backend did not switch");
                this.backends[this.phase] =
                        Backend.REGISTRY.getIdOrThrow(BackendManager.currentBackend()).toString();
                this.selectedBackend = this.phase;
                this.settle = 0;
            }
            if (this.phase == 1 && !VisualizationManager.supportsVisualization(mc.level)) return;
            mc.player.setYRot(180);
            mc.player.setXRot(25);
            if (this.settle++ < 160) return;
            if (this.expectedModels == 0) {
                for (BlockPos pos :
                        BlockPos.betweenClosed(
                                section.minBlockX(),
                                section.minBlockY(),
                                section.minBlockZ(),
                                section.minBlockX() + 15,
                                section.minBlockY() + 15,
                                section.minBlockZ() + 15)) {
                    BlockState state = mc.level.getBlockState(pos);
                    if (!(state.getBlock() instanceof ModelBlock)) continue;
                    this.expectedModels++;
                    var model = mc.getBlockRenderer().getBlockModel(state);
                    this.expectedTotal += model.getQuads(state, null, RandomSource.create()).size();
                    for (var side : net.minecraft.core.Direction.values())
                        this.expectedTotal +=
                                model.getQuads(state, side, RandomSource.create()).size();
                }
            }
            Measurement result = COMPLETED.poll();
            if (result != null) {
                this.waiting = false;
                result.phase = this.phase;
                result.warmup = this.completed < number("warmup");
                this.samples.add(result);
                this.completed++;
                if (!result.success
                        || result.chairCalls != 1
                        || result.bustCalls != number("busts")
                        || result.modelCalls != this.expectedModels) {
                    throw new IllegalStateException(
                            "Incomplete scene: chair calls="
                                    + result.chairCalls
                                    + ", bust calls="
                                    + result.bustCalls);
                }
                if (this.phase == 0
                        && (result.chair != expectedChair
                                || result.bust != expectedBust
                                || result.total != this.expectedTotal)) {
                    throw new IllegalStateException("Native geometry count mismatch");
                }
                if (this.phase == 1 && result.total != 0)
                    throw new IllegalStateException(
                            "Custom models still emitted terrain quads: " + result.total);
                LOGGER.info(
                        "Rebuild phase={} sample={} warmup={} ms={} chair={} bust={}",
                        this.phase,
                        this.completed,
                        result.warmup,
                        result.nanos / 1e6,
                        result.chair,
                        result.bust);
                if (this.completed == number("warmup") + number("samples")) {
                    if (this.phase == 1) {
                        this.measured = true;
                        return;
                    }
                    this.phase = 1;
                    this.completed = 0;
                    return;
                }
            }
            if (!this.waiting) {
                this.waiting = true;
                REQUESTED.set(true);
                mc.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            } else if (REQUESTED.get() && ++this.requestTicks % 10 == 0) {
                // A dirty request made before the preceding upload completes can be coalesced.
                mc.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            }
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private void verifyStateUpdate(Minecraft mc) throws Exception {
        if (++this.stateUpdateTicks > 120)
            throw new IllegalStateException("G3 visual did not follow its changed block state");
        if (this.changedPos == null) {
            for (BlockPos pos :
                    BlockPos.betweenClosed(
                            section.minBlockX(),
                            section.minBlockY(),
                            section.minBlockZ(),
                            section.minBlockX() + 15,
                            section.minBlockY() + 15,
                            section.minBlockZ() + 15)) {
                if (mc.level.getBlockState(pos).is(ModContent.G3_ROUND_CORNER.get())) {
                    this.changedPos = pos.immutable();
                    break;
                }
            }
            if (this.changedPos == null) throw new IllegalStateException("Missing G3 fixture");
            this.previousVisual = visualAt(mc, this.changedPos);
            if (this.previousVisual == null) throw new IllegalStateException("Missing G3 visual");
            this.changedState =
                    ModContent.G3_ROUND_CORNER
                            .get()
                            .defaultBlockState()
                            .setValue(
                                    BlockStateProperties.HORIZONTAL_FACING,
                                    net.minecraft.core.Direction.EAST)
                            .setValue(BlockStateProperties.HALF, Half.TOP)
                            .setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.INNER_LEFT);
            mc.getSingleplayerServer()
                    .execute(
                            () ->
                                    mc.getSingleplayerServer()
                                            .overworld()
                                            .setBlock(this.changedPos, this.changedState, 2));
            return;
        }
        if (mc.level.getBlockState(this.changedPos) != this.changedState) return;
        Object visual = visualAt(mc, this.changedPos);
        if (visual == null || visual == this.previousVisual) return;
        Field state = visual.getClass().getSuperclass().getDeclaredField("blockState");
        state.setAccessible(true);
        if (state.get(visual) != this.changedState)
            throw new IllegalStateException("Recreated G3 visual retained a stale state");
        this.stateUpdateRecreated = true;
        LOGGER.info(
                "G3 visual recreated by Flywheel for changed facing, half and shape: {}",
                this.changedState);
        finish(null);
    }

    private static Object visualAt(Minecraft mc, BlockPos pos) throws Exception {
        Object manager = VisualizationManager.get(mc.level).blockEntities();
        Object storage = manager.getClass().getMethod("getStorage").invoke(manager);
        return storage.getClass()
                .getMethod("visualAtPos", long.class)
                .invoke(storage, pos.asLong());
    }

    private static int quadsPerModel(Minecraft mc, Block block) {
        BlockState state = block.defaultBlockState();
        return mc.getBlockRenderer()
                .getBlockModel(state)
                .getQuads(state, null, RandomSource.create())
                .size();
    }

    private void finish(Throwable failure) {
        this.done = true;
        try {
            Path directory = Path.of(System.getProperty("validation.result"));
            Files.createDirectories(directory);
            StringBuilder csv =
                    new StringBuilder(
                            "phase,warmup,cpu_ms,chair_quads,bust_quads,chair_calls,bust_calls,total_quads,model_calls\n");
            for (Measurement m : this.samples) {
                csv.append(
                        String.format(
                                Locale.ROOT,
                                "%s,%s,%.6f,%d,%d,%d,%d,%d,%d%n",
                                m.phase == 0 ? "native" : "instanced",
                                m.warmup,
                                m.nanos / 1e6,
                                m.chair,
                                m.bust,
                                m.chairCalls,
                                m.bustCalls,
                                m.total,
                                m.modelCalls));
            }
            Files.writeString(directory.resolve("samples.csv"), csv);
            Properties report = new Properties();
            report.setProperty(
                    "passed",
                    Boolean.toString(
                            failure == null
                                    && this.stateUpdateRecreated
                                    && this.samples.size()
                                            == 2 * (number("warmup") + number("samples"))
                                    && this.samples.stream()
                                            .filter(m -> m.phase == 1)
                                            .allMatch(m -> m.total == 0)));
            report.setProperty("failure", failure == null ? "" : failure.toString());
            report.setProperty(
                    "state_update_recreated", Boolean.toString(this.stateUpdateRecreated));
            report.setProperty("samples_per_phase", this.settings.getProperty("samples"));
            report.setProperty("warmup_per_phase", this.settings.getProperty("warmup"));
            report.setProperty("native.backend", String.valueOf(this.backends[0]));
            report.setProperty("instanced.backend", String.valueOf(this.backends[1]));
            for (int p = 0; p < 2; p++) {
                int index = p;
                var measured =
                        this.samples.stream().filter(m -> m.phase == index && !m.warmup).toList();
                if (measured.isEmpty()) continue;
                double[] times =
                        measured.stream().mapToDouble(m -> m.nanos / 1e6).sorted().toArray();
                String prefix = p == 0 ? "native." : "instanced.";
                report.setProperty(
                        prefix + "median_ms",
                        Double.toString(
                                (times[(times.length - 1) / 2] + times[times.length / 2]) / 2));
                report.setProperty(
                        prefix + "p95_ms",
                        Double.toString(times[(int) Math.ceil(times.length * 0.95) - 1]));
                report.setProperty(
                        prefix + "chair_quads", Long.toString(measured.getFirst().chair));
                report.setProperty(prefix + "bust_quads", Long.toString(measured.getFirst().bust));
                report.setProperty(
                        prefix + "total_quads", Long.toString(measured.getFirst().total));
                report.setProperty(
                        prefix + "model_calls", Integer.toString(measured.getFirst().modelCalls));
            }
            try (var writer = Files.newBufferedWriter(directory.resolve("benchmark.properties"))) {
                report.store(writer, "Sodium chunk rebuild CPU measurement");
            }
            LOGGER.info("Benchmark result: {}", report);
            if (failure != null) LOGGER.error("Benchmark failed", failure);
        } catch (Throwable e) {
            LOGGER.error("Cannot write benchmark result", e);
        }
        Minecraft.getInstance().stop();
    }

    private static final class Measurement {
        long nanos, chair, bust, total;
        int chairCalls, bustCalls, phase, modelCalls;
        boolean success, warmup;

        Measurement(long nanos) {
            this.nanos = nanos;
        }
    }
}
