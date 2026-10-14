package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.RavensModels;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/** A flat-shaded regular dodecahedron using the shared white plaster material. */
public final class DodecahedronBakedModel implements BakedModel {
    private static final ResourceLocation GEOMETRY = id("models/mesh/dodecahedron.json");
    private static final ResourceLocation TEXTURE = id("block/white_block");

    private final BakedModel delegate;
    private final List<BakedQuad> mesh;

    private DodecahedronBakedModel(BakedModel delegate, List<BakedQuad> mesh) {
        this.delegate = delegate;
        this.mesh = mesh;
    }

    private static BakedModel bake(
            BakedModel model,
            Function<Material, TextureAtlasSprite> sprites,
            List<MeshQuad> geometry) {
        if (model instanceof DodecahedronBakedModel) {
            return model;
        }

        TextureAtlasSprite sprite =
                sprites.apply(new Material(TextureAtlas.LOCATION_BLOCKS, TEXTURE));
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer emitter = new QuadBakingVertexConsumer();
        emitter.setSprite(sprite);
        emitter.setShade(true);
        emitter.setHasAmbientOcclusion(true);
        for (MeshQuad quad : geometry) {
            for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                MeshVertex vertex = quad.vertices()[vertexIndex];
                emitter.addVertex(vertex.position().x, vertex.position().y, vertex.position().z);
                emitter.setUv(sprite.getU(vertex.u()), sprite.getV(vertex.v()));
                emitter.setNormal(vertex.normal().x, vertex.normal().y, vertex.normal().z);
                emitter.setColor(255, 255, 255, 255);
            }
            emitter.setDirection(quad.nominalFace());
            quads.add(emitter.bakeQuad());
        }
        return new DodecahedronBakedModel(model, List.copyOf(quads));
    }

    private static List<MeshQuad> loadGeometry(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(GEOMETRY);
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.get("uv_handedness").getAsInt() != -1) {
                    throw new IllegalStateException(
                            "Dodecahedron must declare uv_handedness=-1: " + GEOMETRY);
                }
                JsonArray rawVertices = root.getAsJsonArray("vertices");
                Vector3f[] positions = new Vector3f[rawVertices.size()];
                for (int index = 0; index < rawVertices.size(); index++) {
                    JsonArray vertex = rawVertices.get(index).getAsJsonArray();
                    positions[index] =
                            new Vector3f(value(vertex, 0), value(vertex, 1), value(vertex, 2));
                }

                JsonArray rawQuads = root.getAsJsonArray("quads");
                List<MeshQuad> quads = new ArrayList<>(rawQuads.size());
                for (JsonElement element : rawQuads) {
                    quads.add(readQuad(element.getAsJsonObject(), positions));
                }
                if (quads.size() != 60) {
                    throw new IllegalStateException(
                            "Dodecahedron must contain 60 triangles: " + GEOMETRY);
                }
                return List.copyOf(quads);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load dodecahedron geometry " + GEOMETRY, exception);
        }
    }

    private static MeshQuad readQuad(JsonObject quad, Vector3f[] positions) {
        JsonArray indices = quad.getAsJsonArray("vertices");
        JsonArray rawUvs = quad.getAsJsonArray("uv");
        JsonArray rawNormals = quad.getAsJsonArray("normals");
        MeshVertex[] vertices = new MeshVertex[4];
        for (int index = 0; index < 4; index++) {
            JsonArray uv = rawUvs.get(index).getAsJsonArray();
            JsonArray normal = rawNormals.get(index).getAsJsonArray();
            vertices[index] =
                    new MeshVertex(
                            new Vector3f(positions[indices.get(index).getAsInt()]),
                            new Vector3f(value(normal, 0), value(normal, 1), value(normal, 2))
                                    .normalize(),
                            value(uv, 0),
                            value(uv, 1));
        }
        MeshUvHandedness.requireVanilla(
                GEOMETRY,
                vertices[0].position(),
                vertices[0].u(),
                vertices[0].v(),
                vertices[1].position(),
                vertices[1].u(),
                vertices[1].v(),
                vertices[2].position(),
                vertices[2].u(),
                vertices[2].v());
        Vector3f faceNormal =
                new Vector3f(vertices[1].position())
                        .sub(vertices[0].position())
                        .cross(new Vector3f(vertices[2].position()).sub(vertices[0].position()))
                        .normalize();
        return new MeshQuad(
                vertices, Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z));
    }

    private static float value(JsonArray array, int index) {
        return array.get(index).getAsFloat();
    }

    static Function<BakedModel, BakedModel> prepare(
            ResourceManager resources,
            Function<Material, TextureAtlasSprite> sprites,
            String name) {
        DodecahedronBakedModel prepared =
                (DodecahedronBakedModel) bake(null, sprites, loadGeometry(resources));
        return delegate -> new DodecahedronBakedModel(delegate, prepared.mesh);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state, @Nullable Direction side, RandomSource random) {
        return side == null ? (this.mesh) : List.of();
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

    private record MeshQuad(MeshVertex[] vertices, Direction nominalFace) {}

    private record MeshVertex(Vector3f position, Vector3f normal, float u, float v) {}
}
