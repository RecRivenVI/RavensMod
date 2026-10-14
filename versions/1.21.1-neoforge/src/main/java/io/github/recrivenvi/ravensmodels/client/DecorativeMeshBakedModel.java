package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.RavensModels;
import io.github.recrivenvi.ravensmodels.block.PlasticChairBlock;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumMap;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class DecorativeMeshBakedModel implements BakedModel {

    private final BakedModel delegate;
    private final Map<Direction, List<BakedQuad>> meshes;

    private DecorativeMeshBakedModel(BakedModel delegate, Map<Direction, List<BakedQuad>> meshes) {
        this.delegate = delegate;
        this.meshes = meshes;
    }

    private static BakedModel wrap(
            BakedModel model,
            Function<Material, TextureAtlasSprite> sprites,
            String name,
            Geometry geometry,
            Map<String, Map<Direction, List<BakedQuad>>> cache) {
        if (model instanceof DecorativeMeshBakedModel) {
            return model;
        }
        Map<Direction, List<BakedQuad>> meshes =
                cache.computeIfAbsent(
                        name,
                        ignored -> {
                            TextureAtlasSprite sprite =
                                    sprites.apply(
                                            new Material(
                                                    TextureAtlas.LOCATION_BLOCKS,
                                                    id("block/" + name + "_atlas")));
                            Map<Direction, List<BakedQuad>> result = new EnumMap<>(Direction.class);
                            for (Direction direction : Direction.Plane.HORIZONTAL) {
                                float scale =
                                        name.equals("plastic_chair")
                                                ? PlasticChairBlock.MODEL_SCALE
                                                : 1.0f;
                                result.put(
                                        direction, buildMesh(sprite, geometry, direction, scale));
                            }
                            return Map.copyOf(result);
                        });
        return new DecorativeMeshBakedModel(model, meshes);
    }

    private static List<BakedQuad> buildMesh(
            TextureAtlasSprite sprite, Geometry geometry, Direction direction, float scale) {
        int turns =
                switch (direction) {
                    case EAST -> 1;
                    case SOUTH -> 2;
                    case WEST -> 3;
                    default -> 0;
                };
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer emitter = new QuadBakingVertexConsumer();
        emitter.setSprite(sprite);
        emitter.setShade(true);
        emitter.setHasAmbientOcclusion(true);
        for (Triangle triangle : geometry.triangles()) {
            Vector3f normal = rotate(triangle.normal(), turns, false);
            for (int index = 0; index < 4; index++) {
                int vertex = Math.min(index, 2);
                Vector3f scaled =
                        new Vector3f(triangle.positions()[vertex])
                                .sub(0.5f, 0, 0.5f)
                                .mul(scale)
                                .add(0.5f, 0, 0.5f);
                Vector3f position = rotate(scaled, turns, true);
                emitter.addVertex(position.x, position.y, position.z);
                emitter.setUv(sprite.getU(triangle.u()[vertex]), sprite.getV(triangle.v()[vertex]));
                // The normal map is baked in this geometric frame, not a second smooth frame.
                emitter.setNormal(normal.x, normal.y, normal.z);
                emitter.setColor(255, 255, 255, 255);
            }
            emitter.setDirection(Direction.getNearest(normal.x, normal.y, normal.z));
            quads.add(emitter.bakeQuad());
        }
        return List.copyOf(quads);
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
                positions[i] =
                        new Vector3f(
                                p.get(0).getAsFloat(),
                                p.get(1).getAsFloat(),
                                p.get(2).getAsFloat());
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
                MeshUvHandedness.requireVanilla(
                        source, p[0], u[0], v[0], p[1], u[1], v[1], p[2], u[2], v[2]);
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

    static Function<BakedModel, BakedModel> prepare(
            ResourceManager resources,
            Function<Material, TextureAtlasSprite> sprites,
            String name) {
        DecorativeMeshBakedModel prepared =
                (DecorativeMeshBakedModel)
                        wrap(null, sprites, name, loadGeometry(resources, name), new HashMap<>());
        return delegate -> new DecorativeMeshBakedModel(delegate, prepared.meshes);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return side == null
                ? (this.meshes.get(
                        state == null
                                ? Direction.NORTH
                                : state.getValue(BlockStateProperties.HORIZONTAL_FACING)))
                : List.of();
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

    private record Geometry(List<Triangle> triangles) {}

    private record Triangle(Vector3f[] positions, Vector3f normal, float[] u, float[] v) {}
}
