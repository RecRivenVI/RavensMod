package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.AceOfSpadesBlock;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class AceOfSpadesBakedModel implements BakedModel {
    private static final ResourceLocation MODEL = id("item/ace_of_spades");
    private static final ResourceLocation GEOMETRY = id("models/mesh/ace_of_spades.json");

    private final BakedModel delegate;
    private final Meshes meshes;

    private AceOfSpadesBakedModel(BakedModel delegate, Meshes meshes) {
        this.delegate = delegate;
        this.meshes = meshes;
    }

    private static Meshes buildMeshes(TextureAtlasSprite sprite, List<Triangle> triangles) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
        List<List<Vertex>> item = new ArrayList<>();
        for (Triangle triangle : triangles) {
            List<Vertex> vertices = new ArrayList<>(3);
            for (int i = 0; i < 3; i++) {
                Vector3f p = triangle.positions()[i];
                min.min(p);
                max.max(p);
                vertices.add(new Vertex(p, triangle.u()[i], triangle.v()[i]));
            }
            item.add(vertices);
        }

        float scale = AceOfSpadesBlock.DISPLAY_LENGTH / (max.z - min.z);
        Vector3f center = new Vector3f(min).add(max).mul(0.5f);
        List<List<Vertex>> flat = new ArrayList<>();
        for (List<Vertex> triangle : item) {
            flat.add(
                    triangle.stream()
                            .map(
                                    vertex ->
                                            new Vertex(
                                                    new Vector3f(
                                                            0.5f
                                                                    + (center.z
                                                                                    - vertex
                                                                                            .position()
                                                                                            .z)
                                                                            * scale,
                                                            (max.x - vertex.position().x) * scale,
                                                            0.5f
                                                                    + (vertex.position().y
                                                                                    - center.y)
                                                                            * scale),
                                                    vertex.u(),
                                                    vertex.v()))
                            .toList());
        }
        Map<StateKey, List<BakedQuad>> world = new HashMap<>();
        for (AttachFace face : AceOfSpadesBlock.FACE.getPossibleValues()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                List<List<Vertex>> rotated = new ArrayList<>();
                for (List<Vertex> polygon : flat) {
                    rotated.add(
                            polygon.stream()
                                    .map(
                                            vertex -> {
                                                Vector3f p = new Vector3f(vertex.position());
                                                if (face == AttachFace.WALL) {
                                                    // Fold the floor display upright against the
                                                    // support face at Z=1.
                                                    p.set(p.x, p.z, 1 - p.y);
                                                }
                                                return new Vertex(
                                                        rotate(p, facing), vertex.u(), vertex.v());
                                            })
                                    .toList());
                }
                world.put(new StateKey(facing, face), bakeMesh(sprite, rotated));
            }
        }
        return new Meshes(bakeMesh(sprite, item), Map.copyOf(world));
    }

    private static List<BakedQuad> bakeMesh(
            TextureAtlasSprite sprite, List<List<Vertex>> polygons) {
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer emitter = new QuadBakingVertexConsumer();
        emitter.setSprite(sprite);
        emitter.setShade(true);
        emitter.setHasAmbientOcclusion(true);
        for (List<Vertex> polygon : polygons) {
            Vector3f normal =
                    new Vector3f(polygon.get(1).position())
                            .sub(polygon.get(0).position())
                            .cross(
                                    new Vector3f(polygon.get(2).position())
                                            .sub(polygon.get(0).position()));
            if (normal.lengthSquared() < 1.0e-18f) {
                continue;
            }
            normal.normalize();
            for (int i = 0; i < 4; i++) {
                Vertex vertex = polygon.get(Math.min(i, polygon.size() - 1));
                Vector3f position = vertex.position();
                emitter.addVertex(position.x, position.y, position.z);
                emitter.setNormal(normal.x, normal.y, normal.z);
                emitter.setUv(sprite.getU(vertex.u()), sprite.getV(vertex.v()));
                emitter.setColor(255, 255, 255, 255);
            }
            emitter.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
            quads.add(emitter.bakeQuad());
        }
        return List.copyOf(quads);
    }

    private static Vector3f rotate(Vector3f p, Direction facing) {
        return switch (facing) {
            case EAST -> new Vector3f(1 - p.z, p.y, p.x);
            case SOUTH -> new Vector3f(1 - p.x, p.y, 1 - p.z);
            case WEST -> new Vector3f(p.z, p.y, 1 - p.x);
            default -> p;
        };
    }

    private static BakedModel wrap(
            BakedModel model,
            Function<Material, TextureAtlasSprite> sprites,
            List<Triangle> triangles,
            Map<ResourceLocation, Meshes> cache) {
        if (model instanceof AceOfSpadesBakedModel) {
            return model;
        }
        Meshes meshes =
                cache.computeIfAbsent(
                        GEOMETRY,
                        ignored ->
                                buildMeshes(
                                        sprites.apply(
                                                new Material(TextureAtlas.LOCATION_BLOCKS, MODEL)),
                                        triangles));
        return new AceOfSpadesBakedModel(model, meshes);
    }

    private static List<Triangle> loadGeometry(ResourceManager resources) {
        try (Reader reader = resources.getResourceOrThrow(GEOMETRY).openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (root.get("uv_handedness").getAsInt() != -1) {
                throw new IllegalStateException("Expected H=-1 in " + GEOMETRY);
            }
            JsonArray vertices = root.getAsJsonArray("vertices");
            Vector3f[] positions = new Vector3f[vertices.size()];
            for (int i = 0; i < positions.length; i++) {
                JsonArray p = vertices.get(i).getAsJsonArray();
                positions[i] =
                        new Vector3f(
                                p.get(0).getAsFloat(),
                                p.get(1).getAsFloat(),
                                p.get(2).getAsFloat());
            }
            List<Triangle> triangles = new ArrayList<>();
            for (JsonElement element : root.getAsJsonArray("triangles")) {
                JsonObject face = element.getAsJsonObject();
                Vector3f[] p = new Vector3f[3];
                float[] u = new float[3];
                float[] v = new float[3];
                for (int i = 0; i < 3; i++) {
                    p[i] = positions[face.getAsJsonArray("vertices").get(i).getAsInt()];
                    JsonArray uv = face.getAsJsonArray("uv").get(i).getAsJsonArray();
                    u[i] = uv.get(0).getAsFloat();
                    v[i] = uv.get(1).getAsFloat();
                }
                MeshUvHandedness.requireVanilla(
                        GEOMETRY, p[0], u[0], v[0], p[1], u[1], v[1], p[2], u[2], v[2]);
                triangles.add(new Triangle(p, u, v));
            }
            return List.copyOf(triangles);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + GEOMETRY, exception);
        }
    }

    static Function<BakedModel, BakedModel> prepare(
            ResourceManager resources,
            Function<Material, TextureAtlasSprite> sprites,
            String name) {
        AceOfSpadesBakedModel prepared =
                (AceOfSpadesBakedModel)
                        wrap(null, sprites, loadGeometry(resources), new HashMap<>());
        return delegate -> new AceOfSpadesBakedModel(delegate, prepared.meshes);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return side == null
                ? (state == null
                        ? this.meshes.item()
                        : this.meshes
                                .world()
                                .get(
                                        new StateKey(
                                                state.getValue(
                                                        BlockStateProperties.HORIZONTAL_FACING),
                                                state.getValue(AceOfSpadesBlock.FACE))))
                : List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.delegate.getOverrides();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(
            BlockState state, RandomSource random, ModelData data) {
        return this.delegate.getRenderTypes(state, random, data);
    }

    @Override
    public List<RenderType> getRenderTypes(ItemStack stack, boolean fabulous) {
        return this.delegate.getRenderTypes(stack, fabulous);
    }

    private record Triangle(Vector3f[] positions, float[] u, float[] v) {}

    private record Vertex(Vector3f position, float u, float v) {}

    private record StateKey(Direction facing, AttachFace face) {}

    private record Meshes(List<BakedQuad> item, Map<StateKey, List<BakedQuad>> world) {}
}
