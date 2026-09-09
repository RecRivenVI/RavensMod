package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.PlasticChairBlock;
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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class DecorativeMeshBakedModel implements BakedModel, FabricBakedModel {
    private static final List<String> MODELS = List.of("david_bust", "plastic_chair");

    private final BakedModel delegate;
    private final Map<Direction, Mesh> meshes;

    private DecorativeMeshBakedModel(BakedModel delegate, Map<Direction, Mesh> meshes) {
        this.delegate = delegate;
        this.meshes = meshes;
    }

    public static void register() {
        PreparableModelLoadingPlugin.register(
                (resources, executor) -> CompletableFuture.supplyAsync(() -> {
                    Map<String, Geometry> result = new HashMap<>();
                    for (String name : MODELS) {
                        result.put(name, loadGeometry(resources, name));
                    }
                    return Map.copyOf(result);
                }, executor),
                (geometry, context) -> {
                    // A resource reload owns one mesh set per product, shared by its states and item.
                    Map<String, Map<Direction, Mesh>> cache = new ConcurrentHashMap<>();
                    context.modifyModelAfterBake().register((model, bake) -> {
                        ResourceLocation modelId = bake.id();
                        for (String name : MODELS) {
                            if (id("block/" + name).equals(modelId)) {
                                return wrap(model, bake.baker(), name, geometry.get(name), cache);
                            }
                        }
                        return model;
                    });
                    context.modifyBlockModelAfterBake().register((model, bake) -> {
                        ResourceLocation blockId = bake.id().id();
                        for (String name : MODELS) {
                            if (id(name).equals(blockId)) {
                                return wrap(model, bake.baker(), name, geometry.get(name), cache);
                            }
                        }
                        return model;
                    });
                });
    }

    private static BakedModel wrap(
            BakedModel model, ModelBaker baker, String name, Geometry geometry,
            Map<String, Map<Direction, Mesh>> cache) {
        if (model instanceof DecorativeMeshBakedModel) {
            return model;
        }
        Map<Direction, Mesh> meshes = cache.computeIfAbsent(name, ignored -> {
            Renderer renderer = Renderer.get();
            RenderMaterial material = renderer.materialFinder().blendMode(BlendMode.SOLID).find();
            TextureAtlasSprite sprite = baker.sprites().get(
                    new Material(TextureAtlas.LOCATION_BLOCKS, id("block/" + name + "_atlas")));
            Map<Direction, Mesh> result = new EnumMap<>(Direction.class);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                float scale = name.equals("plastic_chair") ? PlasticChairBlock.MODEL_SCALE : 1.0f;
                result.put(direction, buildMesh(renderer, material, sprite, geometry, direction, scale));
            }
            return Map.copyOf(result);
        });
        return new DecorativeMeshBakedModel(model, meshes);
    }

    private static Mesh buildMesh(
            Renderer renderer, RenderMaterial material, TextureAtlasSprite sprite,
            Geometry geometry, Direction direction, float scale) {
        int turns = switch (direction) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        MutableMesh mesh = renderer.mutableMesh();
        QuadEmitter emitter = mesh.emitter();
        for (Triangle triangle : geometry.triangles()) {
            Vector3f normal = rotate(triangle.normal(), turns, false);
            for (int index = 0; index < 4; index++) {
                int vertex = Math.min(index, 2);
                Vector3f scaled = new Vector3f(triangle.positions()[vertex])
                        .sub(0.5f, 0, 0.5f).mul(scale).add(0.5f, 0, 0.5f);
                Vector3f position = rotate(scaled, turns, true);
                emitter.pos(index, position.x, position.y, position.z);
                emitter.uv(index, triangle.u()[vertex], triangle.v()[vertex]);
                // The normal map is baked in this geometric frame, not a second smooth frame.
                emitter.normal(index, normal.x, normal.y, normal.z);
                emitter.color(index, 0xFFFFFFFF);
            }
            emitter.spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED)
                    .material(material)
                    .nominalFace(Direction.getApproximateNearest(normal.x, normal.y, normal.z))
                    .cullFace(null)
                    .emit();
        }
        return mesh.immutableCopy();
    }

    private static Geometry loadGeometry(ResourceManager resources, String name) {
        ResourceLocation source = id("models/mesh/" + name + ".json");
        try (Reader reader = resources.getResourceOrThrow(source).openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.get("uv_handedness").getAsInt() != -1) {
                throw new IllegalStateException("Expected H=-1 in " + source);
            }
            float width = root.get("texture_width").getAsFloat();
            float height = root.get("texture_height").getAsFloat();
            int tile = root.get("atlas_tile_size").getAsInt();
            int gutter = root.get("atlas_gutter").getAsInt();
            int columns = (int) width / tile;
            float span = tile - 2 * gutter - 1;
            JsonArray rawVertices = root.getAsJsonArray("vertices");
            Vector3f[] positions = new Vector3f[rawVertices.size()];
            for (int i = 0; i < positions.length; i++) {
                JsonArray p = rawVertices.get(i).getAsJsonArray();
                positions[i] = new Vector3f(p.get(0).getAsFloat(), p.get(1).getAsFloat(), p.get(2).getAsFloat());
            }
            List<Triangle> triangles = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("triangles")) {
                JsonArray indices = element.getAsJsonArray();
                Vector3f[] p = {
                        positions[indices.get(0).getAsInt()],
                        positions[indices.get(1).getAsInt()],
                        positions[indices.get(2).getAsInt()]
                };
                Vector3f edge1 = new Vector3f(p[1]).sub(p[0]);
                Vector3f edge2 = new Vector3f(p[2]).sub(p[0]);
                Vector3f normal = new Vector3f(edge1).cross(edge2);
                if (normal.lengthSquared() < 1.0e-16f) {
                    throw new IllegalStateException("Degenerate triangle in " + source);
                }
                normal.normalize();
                float tipV = edge1.dot(edge2) / edge1.lengthSquared();
                int index = triangles.size();
                float x = (index % columns) * tile + gutter + 0.5f;
                float y = (index / columns) * tile + gutter + 0.5f;
                float[] u = {x / width, x / width, (x + span) / width};
                float[] v = {y / height, (y + span) / height, (y + tipV * span) / height};
                MeshUvHandedness.requireVanilla(source,
                        p[0], u[0], v[0], p[1], u[1], v[1], p[2], u[2], v[2]);
                triangles.add(new Triangle(p, normal, u, v));
            }
            return new Geometry(List.copyOf(triangles));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + source, exception);
        }
    }

    private static Vector3f rotate(Vector3f source, int turns, boolean position) {
        Vector3f result = new Vector3f(source);
        if (position) {
            result.sub(0.5f, 0.5f, 0.5f);
        }
        for (int i = 0; i < turns; i++) {
            float x = result.x;
            result.x = -result.z;
            result.z = x;
        }
        if (position) {
            result.add(0.5f, 0.5f, 0.5f);
        }
        return result;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.delegate.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
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
            QuadEmitter emitter, BlockAndTintGetter blockView, BlockState state,
            BlockPos pos, Supplier<RandomSource> randomSupplier,
            Predicate<@Nullable Direction> cullTest) {
        this.meshes.get(state.getValue(BlockStateProperties.HORIZONTAL_FACING)).outputTo(emitter);
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
        this.meshes.get(Direction.NORTH).outputTo(emitter);
    }

    private record Geometry(List<Triangle> triangles) {
    }

    private record Triangle(Vector3f[] positions, Vector3f normal, float[] u, float[] v) {
    }
}
