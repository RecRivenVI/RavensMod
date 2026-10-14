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

/** A reload-safe, chunk-baked sphere mesh shared by the white and mirror materials. */
public final class SphereBakedModel implements BakedModel {
    private static final ResourceLocation GEOMETRY = id("models/mesh/sphere.json");

    private final BakedModel delegate;
    private final List<BakedQuad> mesh;

    private SphereBakedModel(BakedModel delegate, List<BakedQuad> mesh) {
        this.delegate = delegate;
        this.mesh = mesh;
    }

    private static BakedModel bake(
            BakedModel model,
            Function<Material, TextureAtlasSprite> sprites,
            List<MeshQuad> geometry,
            ResourceLocation texture) {
        if (model instanceof SphereBakedModel) {
            return model;
        }

        TextureAtlasSprite sprite =
                sprites.apply(new Material(TextureAtlas.LOCATION_BLOCKS, texture));
        List<BakedQuad> quads = new ArrayList<>();
        QuadBakingVertexConsumer emitter = new QuadBakingVertexConsumer();
        emitter.setSprite(sprite);
        emitter.setShade(true);
        emitter.setHasAmbientOcclusion(true);

        for (MeshQuad quad : geometry) {
            for (int vertexIndex = 0; vertexIndex < quad.vertices().length; vertexIndex++) {
                MeshVertex vertex = quad.vertices()[vertexIndex];
                emitter.addVertex(vertex.x(), vertex.y(), vertex.z());
                emitter.setUv(sprite.getU(vertex.u()), sprite.getV(vertex.v()));
                emitter.setNormal(vertex.normalX(), vertex.normalY(), vertex.normalZ());
                emitter.setColor(255, 255, 255, 255);
            }

            emitter.setDirection(quad.nominalFace());
            quads.add(emitter.bakeQuad());
        }

        return new SphereBakedModel(model, List.copyOf(quads));
    }

    private static List<MeshQuad> loadGeometry(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(GEOMETRY);
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                float textureWidth = root.get("texture_width").getAsFloat();
                float textureHeight = root.get("texture_height").getAsFloat();
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
                    quads.add(
                            readQuad(
                                    element.getAsJsonObject(),
                                    positions,
                                    textureWidth,
                                    textureHeight));
                }

                if (positions.length == 0 || quads.isEmpty()) {
                    throw new IllegalStateException("Sphere mesh is empty: " + GEOMETRY);
                }
                return List.copyOf(quads);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to load sphere geometry " + GEOMETRY, exception);
        }
    }

    private static MeshQuad readQuad(
            JsonObject quad, Vector3f[] positions, float textureWidth, float textureHeight) {
        JsonArray indices = quad.getAsJsonArray("vertices");
        JsonArray rawUvs = quad.getAsJsonArray("uv");
        MeshVertex[] vertices = new MeshVertex[4];
        for (int index = 0; index < vertices.length; index++) {
            Vector3f position = positions[indices.get(index).getAsInt()];
            JsonArray uv = rawUvs.get(index).getAsJsonArray();
            Vector3f normal = new Vector3f(position).sub(0.5f, 0.5f, 0.5f).normalize();
            vertices[index] =
                    new MeshVertex(
                            position.x,
                            position.y,
                            position.z,
                            value(uv, 0) / textureWidth,
                            value(uv, 1) / textureHeight,
                            normal.x,
                            normal.y,
                            normal.z);
        }

        Vector3f edge1 =
                new Vector3f(positions[indices.get(1).getAsInt()])
                        .sub(positions[indices.get(0).getAsInt()]);
        Vector3f edge2 =
                new Vector3f(positions[indices.get(2).getAsInt()])
                        .sub(positions[indices.get(0).getAsInt()]);
        MeshUvHandedness.requireVanilla(
                GEOMETRY,
                positions[indices.get(0).getAsInt()],
                vertices[0].u,
                vertices[0].v,
                positions[indices.get(1).getAsInt()],
                vertices[1].u,
                vertices[1].v,
                positions[indices.get(2).getAsInt()],
                vertices[2].u,
                vertices[2].v);
        Vector3f faceNormal = edge1.cross(edge2);
        if (faceNormal.lengthSquared() < 1.0e-12f) {
            edge1.set(positions[indices.get(3).getAsInt()])
                    .sub(positions[indices.get(2).getAsInt()]);
            edge2.set(positions[indices.get(0).getAsInt()])
                    .sub(positions[indices.get(2).getAsInt()]);
            faceNormal.set(edge1.cross(edge2));
        }
        faceNormal.normalize();
        Direction nominalFace = Direction.getNearest(faceNormal.x, faceNormal.y, faceNormal.z);
        return new MeshQuad(vertices, nominalFace);
    }

    private static float value(JsonArray array, int index) {
        return array.get(index).getAsFloat();
    }

    static Function<BakedModel, BakedModel> prepare(
            ResourceManager resources,
            Function<Material, TextureAtlasSprite> sprites,
            String name) {
        SphereBakedModel prepared =
                (SphereBakedModel)
                        bake(
                                null,
                                sprites,
                                loadGeometry(resources),
                                id("block/" + name + "_atlas"));
        return delegate -> new SphereBakedModel(delegate, prepared.mesh);
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

    private record MeshVertex(
            float x,
            float y,
            float z,
            float u,
            float v,
            float normalX,
            float normalY,
            float normalZ) {}
}
