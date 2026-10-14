package io.github.recrivenvi.ravensmodels.probe;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.recrivenvi.ravensmodels.ModContent;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.DecorativeModelBlockEntity;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Ordinary BER is an isolated experiment, never an extra product path. */
public final class FallbackRenderProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger(RavensModels.MOD_ID + "_probe");
    private static final SectionPos SECTION = SectionPos.of(5, -4, -1);
    private static final ThreadLocal<Rebuild> CURRENT = new ThreadLocal<>();
    private static final ConcurrentLinkedQueue<Rebuild> COMPLETED = new ConcurrentLinkedQueue<>();
    private static final AtomicBoolean REQUESTED = new AtomicBoolean();
    private static volatile boolean active;
    private static volatile boolean ber;
    private final Properties settings = new Properties();
    private final List<Run> runs = new ArrayList<>();
    private final ArrayDeque<Frame> pending = new ArrayDeque<>();
    private List<Block> blocks;
    private int[] copies;
    private Run run;
    private Frame drawing;
    private long start = System.nanoTime();
    private long lastFrameStart;
    private boolean opened, done, waiting, measuringFrames;
    private volatile boolean fixtureReady;
    private int stage, settle, rebuilt, framesSubmitted, frameWarmup, retry;
    private long expectedQuads;

    public FallbackRenderProbe(IEventBus bus) throws Exception {
        if (ModList.get().isLoaded("flywheel") || ModList.get().isLoaded("colorwheel")) {
            throw new IllegalStateException(
                    "Fallback comparison requires Flywheel and Colorwheel to be absent");
        }
        active = true;
        try (var reader =
                Files.newBufferedReader(
                        Path.of(System.getProperty("validation.task"), "scene.properties"))) {
            settings.load(reader);
        }
        copies =
                java.util.Arrays.stream(settings.getProperty("copies").split(","))
                        .mapToInt(Integer::parseInt)
                        .toArray();
        bus.addListener(EventPriority.LOWEST, this::renderers);
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::preFrame);
        NeoForge.EVENT_BUS.addListener(this::postFrame);
    }

    private int number(String key) {
        return Integer.parseInt(settings.getProperty(key));
    }

    public static boolean useBer() {
        return active && ber;
    }

    private void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModContent.CHAIR_MODEL.get(), context -> this::render);
        event.registerBlockEntityRenderer(
                ModContent.DAVID_BUST_MODEL.get(), context -> this::render);
        event.registerBlockEntityRenderer(ModContent.MODEL_BLOCK.get(), context -> this::render);
    }

    private void render(
            DecorativeModelBlockEntity entity,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        if (!useBer()) return;
        if (drawing != null) drawing.draws++;
        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        entity.getBlockState(),
                        poses,
                        buffers,
                        light,
                        overlay,
                        ModelData.EMPTY,
                        null);
    }

    public static void begin(Object task) {
        if (!active || !REQUESTED.get()) return;
        try {
            Field field = task.getClass().getDeclaredField("renderContext");
            field.setAccessible(true);
            Object context = field.get(task);
            if (SECTION.equals(context.getClass().getMethod("getOrigin").invoke(context))
                    && REQUESTED.compareAndSet(true, false))
                CURRENT.set(new Rebuild(System.nanoTime()));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void quads(int count) {
        Rebuild r = CURRENT.get();
        if (r != null) r.quads += count;
    }

    public static void end(boolean success) {
        Rebuild r = CURRENT.get();
        if (r == null) return;
        CURRENT.remove();
        r.nanos = System.nanoTime() - r.nanos;
        r.success = success;
        COMPLETED.add(r);
    }

    private void tick(ClientTickEvent.Post event) {
        if (done) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (System.nanoTime() - start > 900_000_000_000L)
                throw new IllegalStateException("Fallback benchmark timeout");
            drainQueries();
            if (!opened && mc.screen instanceof TitleScreen) {
                opened = true;
                mc.options.renderDistance().set(4);
                mc.options.enableVsync().set(false);
                mc.options.framerateLimit().set(260);
                mc.options.pauseOnLostFocus = false;
                var flows = mc.createWorldOpenFlows();
                if (Files.isDirectory(
                        mc.gameDirectory.toPath().resolve("saves/fallback_benchmark")))
                    flows.openWorld("fallback_benchmark", () -> {});
                else
                    flows.createFreshLevel(
                            "fallback_benchmark",
                            new LevelSettings(
                                    "Fallback Benchmark",
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
            if (mc.level == null || mc.player == null || mc.getSingleplayerServer() == null) return;
            mc.player.setYRot(180);
            mc.player.setXRot(25);
            if (blocks == null) {
                blocks = ModContent.modelBlocks();
                if (blocks.size() != 9)
                    throw new IllegalStateException(
                            "Custom model coverage changed: " + blocks.size());
                prepareScene(mc);
                return;
            }
            if (!fixtureReady || ++settle < 120) return;
            if (run == null) {
                ber = stage % 2 == 1;
                run = new Run(copies[stage / 2], ber ? "ber" : "baked");
                runs.add(run);
                expectedQuads = 0;
                for (int i = 0; i < blocks.size() * run.copies; i++) {
                    BlockPos pos = position(i);
                    if (!(mc.level.getBlockEntity(pos) instanceof DecorativeModelBlockEntity))
                        throw new IllegalStateException("Missing render anchor at " + pos);
                    BlockState state = mc.level.getBlockState(pos);
                    var model = mc.getBlockRenderer().getBlockModel(state);
                    expectedQuads += model.getQuads(state, null, RandomSource.create()).size();
                    for (Direction side : Direction.values())
                        expectedQuads += model.getQuads(state, side, RandomSource.create()).size();
                }
                mc.levelRenderer.allChanged();
                rebuilt = 0;
                waiting = false;
                framesSubmitted = 0;
                frameWarmup = 0;
                LOGGER.info(
                        "Fallback scene={} mode={} models={} full_quads={}",
                        run.copies,
                        run.mode,
                        blocks.size() * run.copies,
                        expectedQuads);
            }
            if (rebuilt < number("warmup_rebuilds") + number("rebuilds")) {
                Rebuild r = COMPLETED.poll();
                if (r != null) {
                    waiting = false;
                    if (!r.success || r.quads != (ber ? 0 : expectedQuads))
                        throw new IllegalStateException(
                                "Terrain geometry mismatch: "
                                        + r.quads
                                        + " expected="
                                        + (ber ? 0 : expectedQuads));
                    if (rebuilt++ >= number("warmup_rebuilds")) run.rebuilds.add(r);
                }
                if (rebuilt < number("warmup_rebuilds") + number("rebuilds")) {
                    if (!waiting) {
                        waiting = true;
                        REQUESTED.set(true);
                    }
                    if (REQUESTED.get() && retry++ % 5 == 0)
                        mc.levelRenderer.setSectionDirty(SECTION.x(), SECTION.y(), SECTION.z());
                    return;
                }
                settle = 60;
                return;
            }
            measuringFrames = framesSubmitted < number("frames");
            if (measuringFrames || !pending.isEmpty()) return;
            LOGGER.info(
                    "Fallback completed scene={} mode={} cpu={}ms gpu={}ms",
                    run.copies,
                    run.mode,
                    median(run.frames.stream().mapToDouble(f -> f.cpu / 1e6).toArray()),
                    median(run.frames.stream().mapToDouble(f -> f.gpu / 1e6).toArray()));
            if (++stage == copies.length * 2) {
                finish(null);
                return;
            }
            run = null;
            settle = 0;
            if (stage % 2 == 0) prepareScene(mc);
        } catch (Throwable failure) {
            finish(failure);
        }
    }

    private static BlockPos position(int i) {
        return new BlockPos(
                SECTION.minBlockX() + 1 + i % 6 * 2,
                SECTION.minBlockY() + 4,
                SECTION.minBlockZ() + 1 + i / 6 * 2);
    }

    private void prepareScene(Minecraft mc) {
        fixtureReady = false;
        int sceneCopies = copies[stage / 2];
        mc.getSingleplayerServer()
                .execute(
                        () -> {
                            var level = mc.getSingleplayerServer().overworld();
                            level.getGameRules()
                                    .getRule(GameRules.RULE_DAYLIGHT)
                                    .set(false, mc.getSingleplayerServer());
                            level.getGameRules()
                                    .getRule(GameRules.RULE_RANDOMTICKING)
                                    .set(0, mc.getSingleplayerServer());
                            level.setDayTime(6000);
                            for (int i = 0;
                                    i
                                            < java.util.Arrays.stream(copies).max().orElseThrow()
                                                    * blocks.size();
                                    i++)
                                level.setBlockAndUpdate(
                                        position(i), Blocks.AIR.defaultBlockState());
                            for (int i = 0; i < blocks.size() * sceneCopies; i++) {
                                BlockState state =
                                        blocks.get(i % blocks.size()).defaultBlockState();
                                if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING))
                                    state =
                                            state.setValue(
                                                    BlockStateProperties.HORIZONTAL_FACING,
                                                    Direction.from2DDataValue(i / blocks.size()));
                                level.setBlockAndUpdate(position(i), state);
                            }
                            var player =
                                    mc.getSingleplayerServer()
                                            .getPlayerList()
                                            .getPlayer(mc.player.getUUID());
                            player.teleportTo(
                                    SECTION.minBlockX() + 7,
                                    SECTION.minBlockY() + 7,
                                    SECTION.minBlockZ() + 18);
                            fixtureReady = true;
                        });
    }

    private void preFrame(RenderFrameEvent.Pre event) {
        if (!measuringFrames || run == null || done || pending.size() > 32) return;
        long now = System.nanoTime();
        Frame f = new Frame(run);
        f.start = now;
        f.interval = lastFrameStart == 0 ? 0 : now - lastFrameStart;
        lastFrameStart = now;
        f.warmup = frameWarmup++ < number("warmup_frames");
        f.queryStart = GL15.glGenQueries();
        f.queryEnd = GL15.glGenQueries();
        GL33.glQueryCounter(f.queryStart, GL33.GL_TIMESTAMP);
        drawing = f;
    }

    private void postFrame(RenderFrameEvent.Post event) {
        Frame f = drawing;
        if (f == null) return;
        drawing = null;
        f.cpu = System.nanoTime() - f.start;
        GL33.glQueryCounter(f.queryEnd, GL33.GL_TIMESTAMP);
        pending.add(f);
        if (!f.warmup) {
            framesSubmitted++;
            if (framesSubmitted == number("frames")) measuringFrames = false;
        }
        drainQueries();
    }

    private void drainQueries() {
        while (!pending.isEmpty()
                && GL15.glGetQueryObjecti(pending.peek().queryEnd, GL15.GL_QUERY_RESULT_AVAILABLE)
                        != 0) {
            Frame f = pending.remove();
            f.gpu =
                    GL33.glGetQueryObjectui64(f.queryEnd, GL15.GL_QUERY_RESULT)
                            - GL33.glGetQueryObjectui64(f.queryStart, GL15.GL_QUERY_RESULT);
            GL15.glDeleteQueries(f.queryStart);
            GL15.glDeleteQueries(f.queryEnd);
            if (!f.warmup) f.run.frames.add(f);
        }
    }

    private void finish(Throwable failure) {
        done = true;
        measuringFrames = false;
        try {
            Properties report = new Properties();
            report.setProperty(
                    "passed",
                    Boolean.toString(
                            failure == null
                                    && runs.size() == copies.length * 2
                                    && runs.stream()
                                            .allMatch(
                                                    r ->
                                                            r.frames.size() == number("frames")
                                                                    && r.rebuilds.size()
                                                                            == number("rebuilds")
                                                                    && r.frames.stream()
                                                                            .allMatch(
                                                                                    f ->
                                                                                            r.mode
                                                                                                            .equals(
                                                                                                                    "baked")
                                                                                                    ? f.draws
                                                                                                            == 0
                                                                                                    : f.draws
                                                                                                            >= blocks
                                                                                                                            .size()
                                                                                                                    * r.copies))));
            report.setProperty("failure", failure == null ? "" : failure.toString());
            report.setProperty(
                    "flywheel_installed", Boolean.toString(ModList.get().isLoaded("flywheel")));
            StringBuilder frames =
                    new StringBuilder("copies,mode,cpu_ms,gpu_ms,interval_ms,ber_draws\n");
            StringBuilder rebuilds = new StringBuilder("copies,mode,task_ms,terrain_quads\n");
            for (Run r : runs) {
                String p = r.copies + "." + r.mode + ".";
                report.setProperty(
                        p + "cpu_median_ms",
                        Double.toString(
                                median(r.frames.stream().mapToDouble(f -> f.cpu / 1e6).toArray())));
                report.setProperty(
                        p + "cpu_p95_ms",
                        Double.toString(
                                p95(r.frames.stream().mapToDouble(f -> f.cpu / 1e6).toArray())));
                report.setProperty(
                        p + "gpu_median_ms",
                        Double.toString(
                                median(r.frames.stream().mapToDouble(f -> f.gpu / 1e6).toArray())));
                report.setProperty(
                        p + "interval_median_ms",
                        Double.toString(
                                median(
                                        r.frames.stream()
                                                .mapToDouble(f -> f.interval / 1e6)
                                                .toArray())));
                report.setProperty(
                        p + "rebuild_median_ms",
                        Double.toString(
                                median(
                                        r.rebuilds.stream()
                                                .mapToDouble(r0 -> r0.nanos / 1e6)
                                                .toArray())));
                report.setProperty(
                        p + "terrain_quads",
                        r.rebuilds.isEmpty()
                                ? "missing"
                                : Long.toString(r.rebuilds.getFirst().quads));
                report.setProperty(
                        p + "ber_draws_min",
                        Integer.toString(
                                r.frames.stream().mapToInt(f -> f.draws).min().orElse(-1)));
                for (Frame f : r.frames)
                    frames.append(
                            String.format(
                                    Locale.ROOT,
                                    "%d,%s,%.6f,%.6f,%.6f,%d%n",
                                    r.copies,
                                    r.mode,
                                    f.cpu / 1e6,
                                    f.gpu / 1e6,
                                    f.interval / 1e6,
                                    f.draws));
                for (Rebuild r0 : r.rebuilds)
                    rebuilds.append(
                            String.format(
                                    Locale.ROOT,
                                    "%d,%s,%.6f,%d%n",
                                    r.copies,
                                    r.mode,
                                    r0.nanos / 1e6,
                                    r0.quads));
            }
            Path out = Path.of(System.getProperty("validation.result"));
            Files.createDirectories(out);
            Files.writeString(out.resolve("frames.csv"), frames);
            Files.writeString(out.resolve("rebuilds.csv"), rebuilds);
            try (var writer = Files.newBufferedWriter(out.resolve("fallback.properties"))) {
                report.store(writer, "No Flywheel fallback comparison");
            }
            LOGGER.info("Fallback result: {}", report);
            if (failure != null) LOGGER.error("Fallback failed", failure);
        } catch (Throwable e) {
            LOGGER.error("Cannot write fallback report", e);
        }
        Minecraft.getInstance().stop();
    }

    private static double median(double[] v) {
        java.util.Arrays.sort(v);
        return v.length == 0 ? 0 : (v[(v.length - 1) / 2] + v[v.length / 2]) / 2;
    }

    private static double p95(double[] v) {
        java.util.Arrays.sort(v);
        return v.length == 0 ? 0 : v[(int) Math.ceil(v.length * .95) - 1];
    }

    private static final class Rebuild {
        long nanos, quads;
        boolean success;

        Rebuild(long nanos) {
            this.nanos = nanos;
        }
    }

    private static final class Run {
        final int copies;
        final String mode;
        final List<Rebuild> rebuilds = new ArrayList<>();
        final List<Frame> frames = new ArrayList<>();

        Run(int copies, String mode) {
            this.copies = copies;
            this.mode = mode;
        }
    }

    private static final class Frame {
        final Run run;
        long start, cpu, gpu, interval;
        int queryStart, queryEnd, draws;
        boolean warmup;

        Frame(Run run) {
            this.run = run;
        }
    }
}
