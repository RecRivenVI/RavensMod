package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.RavensModels;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Converts the authored throne geometry directly into a reload-safe Fabric mesh. */
public final class ThroneBakedModel implements BakedModel, FabricBakedModel {
    private static final ResourceLocation GEOMETRY = id("geo/shattered_throne.geo.json");
    private static final ResourceLocation BLOCK = id("shattered_throne");
    private static final ResourceLocation MODEL = id("block/shattered_throne");
    private static final ResourceLocation TEXTURE = id("block/mirror_block");

    private static final Face[] CUBE_FACES = {
            new Face(new int[] {0, 1, 3, 2}, new Vector3f(-1, 0, 0)),
            new Face(new int[] {5, 4, 6, 7}, new Vector3f(1, 0, 0)),
            new Face(new int[] {1, 0, 4, 5}, new Vector3f(0, -1, 0)),
            new Face(new int[] {2, 3, 7, 6}, new Vector3f(0, 1, 0)),
            new Face(new int[] {4, 0, 2, 6}, new Vector3f(0, 0, -1)),
            new Face(new int[] {1, 5, 7, 3}, new Vector3f(0, 0, 1))
    };
    private static final float[][] VANILLA_UV = {
            {0, 1}, {1, 1}, {1, 0}, {0, 0}
    };

    private final BakedModel delegate;
    private final Map<Direction, Mesh> meshes;

    private ThroneBakedModel(BakedModel delegate, Map<Direction, Mesh> meshes) {
        this.delegate = delegate;
        this.meshes = meshes;
    }

    public static void register() {
        PreparableModelLoadingPlugin.register(
                (resourceManager, executor) -> CompletableFuture.supplyAsync(
                        () -> loadCubes(resourceManager), executor),
                (cubes, context) -> {
                    context.modifyModelAfterBake().register((model, bakeContext) ->
                            MODEL.equals(bakeContext.id())
                                    ? bake(model, bakeContext.baker(), cubes)
                                    : model);
                    context.modifyBlockModelAfterBake().register((model, bakeContext) ->
                            BLOCK.equals(bakeContext.id().id())
                                    ? bake(model, bakeContext.baker(), cubes)
                                    : model);
                });
    }

    private static BakedModel bake(BakedModel model, ModelBaker baker, List<Cube> cubes) {
        if (model instanceof ThroneBakedModel) {
            return model;
        }

        TextureAtlasSprite sprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, TEXTURE));
        Renderer renderer = Renderer.get();
        RenderMaterial material = renderer.materialFinder().blendMode(BlendMode.SOLID).find();
        Map<Direction, Mesh> meshes = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            meshes.put(direction, buildMesh(renderer, material, sprite, cubes, direction));
        }
        return new ThroneBakedModel(model, Map.copyOf(meshes));
    }

    private static Mesh buildMesh(
            Renderer renderer,
            RenderMaterial material,
            TextureAtlasSprite sprite,
            List<Cube> cubes,
            Direction direction) {
        MutableMesh mutableMesh = renderer.mutableMesh();
        QuadEmitter emitter = mutableMesh.emitter();
        int quarterTurns = switch (direction) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };

        for (Cube cube : cubes) {
            Vector3f[] corners = cube.corners();
            for (Face face : CUBE_FACES) {
                Vector3f normal = cube.rotation().transform(new Vector3f(face.normal()));
                rotateY(normal, quarterTurns, false).normalize();
                Vector3f[] positions = new Vector3f[4];
                for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                    Vector3f position = cube.transform(corners[face.corners()[vertexIndex]]);
                    rotateY(position, quarterTurns, true);
                    positions[vertexIndex] = position;
                    emitter.pos(vertexIndex, position.x, position.y, position.z);
                    emitter.uv(vertexIndex, VANILLA_UV[vertexIndex][0], VANILLA_UV[vertexIndex][1]);
                    emitter.normal(vertexIndex, normal.x, normal.y, normal.z);
                    emitter.color(vertexIndex, 0xFFFFFFFF);
                }

                MeshUvHandedness.requireVanilla(
                        GEOMETRY,
                        positions[0], VANILLA_UV[0][0], VANILLA_UV[0][1],
                        positions[1], VANILLA_UV[1][0], VANILLA_UV[1][1],
                        positions[2], VANILLA_UV[2][0], VANILLA_UV[2][1]);
                emitter
                        .spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED)
                        .material(material)
                        .nominalFace(Direction.getApproximateNearest(normal.x, normal.y, normal.z))
                        .cullFace(null)
                        .emit();
            }
        }
        return mutableMesh.immutableCopy();
    }

    private static List<Cube> loadCubes(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(GEOMETRY);
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
                if (geometries.size() != 1) {
                    throw new IllegalStateException("Throne geometry must contain exactly one model: " + GEOMETRY);
                }

                JsonArray bones = geometries.get(0).getAsJsonObject().getAsJsonArray("bones");
                if (bones.size() != 1) {
                    throw new IllegalStateException("Throne geometry must contain exactly one root bone: " + GEOMETRY);
                }
                JsonObject bone = bones.get(0).getAsJsonObject();
                requireZeroTransform(bone, "pivot");
                requireZeroTransform(bone, "rotation");

                JsonArray rawCubes = bone.getAsJsonArray("cubes");
                List<Cube> cubes = new ArrayList<>(rawCubes.size());
                for (JsonElement element : rawCubes) {
                    JsonObject cube = element.getAsJsonObject();
                    cubes.add(new Cube(
                            vector(cube, "origin"),
                            vector(cube, "size"),
                            vectorOrZero(cube, "pivot"),
                            vectorOrZero(cube, "rotation")));
                }
                if (cubes.isEmpty()) {
                    throw new IllegalStateException("Throne geometry is empty: " + GEOMETRY);
                }
                return List.copyOf(cubes);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load throne geometry " + GEOMETRY, exception);
        }
    }

    private static void requireZeroTransform(JsonObject object, String member) {
        Vector3f value = vectorOrZero(object, member);
        if (value.lengthSquared() > 1.0e-12f) {
            throw new IllegalStateException("Unsupported throne root bone " + member + " in " + GEOMETRY);
        }
    }

    private static Vector3f vector(JsonObject object, String member) {
        JsonArray value = object.getAsJsonArray(member);
        if (value == null || value.size() != 3) {
            throw new IllegalStateException("Missing three-component " + member + " in " + GEOMETRY);
        }
        return new Vector3f(value(value, 0), value(value, 1), value(value, 2));
    }

    private static Vector3f vectorOrZero(JsonObject object, String member) {
        return object.has(member) ? vector(object, member) : new Vector3f();
    }

    private static float value(JsonArray array, int index) {
        return array.get(index).getAsFloat();
    }

    private static Vector3f rotateY(Vector3f vector, int quarterTurns, boolean aroundCenter) {
        if (aroundCenter) {
            vector.sub(0.5f, 0.5f, 0.5f);
        }
        for (int index = 0; index < quarterTurns; index++) {
            float x = vector.x;
            vector.x = -vector.z;
            vector.z = x;
        }
        if (aroundCenter) {
            vector.add(0.5f, 0.5f, 0.5f);
        }
        return vector;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.delegate.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.delegate.usesBlockLight();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.delegate.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.delegate.getTransforms();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(
            QuadEmitter emitter,
            BlockAndTintGetter blockView,
            BlockState state,
            BlockPos pos,
            Supplier<RandomSource> randomSupplier,
            Predicate<@Nullable Direction> cullTest) {
        Direction direction = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
        this.meshes.get(direction).outputTo(emitter);
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
        this.meshes.get(Direction.NORTH).outputTo(emitter);
    }

    private record Face(int[] corners, Vector3f normal) {
    }

    private record Cube(Vector3f origin, Vector3f size, Vector3f pivot, Vector3f authoredRotation) {
        Quaternionf rotation() {
            return new Quaternionf()
                    .rotationZ((float)Math.toRadians(this.authoredRotation.x))
                    .rotateY((float)Math.toRadians(-this.authoredRotation.y))
                    .rotateX((float)Math.toRadians(-this.authoredRotation.z));
        }

        Vector3f[] corners() {
            float minX = -(this.origin.x + this.size.x) / 16.0f;
            float maxX = minX + this.size.x / 16.0f;
            float minY = this.origin.y / 16.0f;
            float maxY = minY + this.size.y / 16.0f;
            float minZ = this.origin.z / 16.0f;
            float maxZ = minZ + this.size.z / 16.0f;
            return new Vector3f[] {
                    new Vector3f(minX, minY, minZ),
                    new Vector3f(minX, minY, maxZ),
                    new Vector3f(minX, maxY, minZ),
                    new Vector3f(minX, maxY, maxZ),
                    new Vector3f(maxX, minY, minZ),
                    new Vector3f(maxX, minY, maxZ),
                    new Vector3f(maxX, maxY, minZ),
                    new Vector3f(maxX, maxY, maxZ)
            };
        }

        Vector3f transform(Vector3f source) {
            Vector3f convertedPivot = new Vector3f(-this.pivot.x, this.pivot.y, this.pivot.z).div(16.0f);
            return this.rotation().transform(new Vector3f(source).sub(convertedPivot))
                    .add(convertedPivot)
                    .add(0.5f, 0, 0.5f);
        }
    }
}
