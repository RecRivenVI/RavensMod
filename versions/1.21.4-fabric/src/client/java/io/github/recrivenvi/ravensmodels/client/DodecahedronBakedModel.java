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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** A flat-shaded regular dodecahedron with no PBR material maps. */
public final class DodecahedronBakedModel implements BakedModel, FabricBakedModel {
    private static final ResourceLocation GEOMETRY = id("models/mesh/dodecahedron.json");
    private static final ResourceLocation BLOCK = id("dodecahedron");
    private static final ResourceLocation MODEL = id("block/dodecahedron");
    private static final ResourceLocation TEXTURE = id("block/white_block");

    private final BakedModel delegate;
    private final Mesh mesh;

    private DodecahedronBakedModel(BakedModel delegate, Mesh mesh) {
        this.delegate = delegate;
        this.mesh = mesh;
    }

    public static void register() {
        PreparableModelLoadingPlugin.register(
                (resourceManager, executor) -> CompletableFuture.supplyAsync(
                        () -> loadGeometry(resourceManager), executor),
                (geometry, context) -> {
                    context.modifyModelAfterBake().register((model, bakeContext) ->
                            MODEL.equals(bakeContext.id())
                                    ? bake(model, bakeContext.baker(), geometry)
                                    : model);
                    context.modifyBlockModelAfterBake().register((model, bakeContext) ->
                            BLOCK.equals(bakeContext.id().id())
                                    ? bake(model, bakeContext.baker(), geometry)
                                    : model);
                });
    }

    private static BakedModel bake(BakedModel model, ModelBaker baker, List<MeshQuad> geometry) {
        if (model instanceof DodecahedronBakedModel) {
            return model;
        }

        TextureAtlasSprite sprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, TEXTURE));
        Renderer renderer = Renderer.get();
        MutableMesh mutableMesh = renderer.mutableMesh();
        QuadEmitter emitter = mutableMesh.emitter();
        RenderMaterial material = renderer.materialFinder().blendMode(BlendMode.SOLID).find();
        for (MeshQuad quad : geometry) {
            for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                MeshVertex vertex = quad.vertices()[vertexIndex];
                emitter.pos(vertexIndex, vertex.position().x, vertex.position().y, vertex.position().z);
                emitter.uv(vertexIndex, vertex.u(), vertex.v());
                emitter.normal(vertexIndex, vertex.normal().x, vertex.normal().y, vertex.normal().z);
                emitter.color(vertexIndex, 0xFFFFFFFF);
            }
            emitter
                    .spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED)
                    .material(material)
                    .nominalFace(quad.nominalFace())
                    .cullFace(null)
                    .emit();
        }
        return new DodecahedronBakedModel(model, mutableMesh.immutableCopy());
    }

    private static List<MeshQuad> loadGeometry(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(GEOMETRY);
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.get("uv_handedness").getAsInt() != -1) {
                    throw new IllegalStateException("Dodecahedron must declare uv_handedness=-1: " + GEOMETRY);
                }
                if (root.get("pbr").getAsBoolean()) {
                    throw new IllegalStateException("Dodecahedron must not use PBR maps: " + GEOMETRY);
                }

                JsonArray rawVertices = root.getAsJsonArray("vertices");
                Vector3f[] positions = new Vector3f[rawVertices.size()];
                for (int index = 0; index < rawVertices.size(); index++) {
                    JsonArray vertex = rawVertices.get(index).getAsJsonArray();
                    positions[index] = new Vector3f(value(vertex, 0), value(vertex, 1), value(vertex, 2));
                }

                JsonArray rawQuads = root.getAsJsonArray("quads");
                List<MeshQuad> quads = new ArrayList<>(rawQuads.size());
                for (JsonElement element : rawQuads) {
                    quads.add(readQuad(element.getAsJsonObject(), positions));
                }
                if (quads.size() != 60) {
                    throw new IllegalStateException("Dodecahedron must contain 60 triangles: " + GEOMETRY);
                }
                return List.copyOf(quads);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load dodecahedron geometry " + GEOMETRY, exception);
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
            vertices[index] = new MeshVertex(
                    new Vector3f(positions[indices.get(index).getAsInt()]),
                    new Vector3f(value(normal, 0), value(normal, 1), value(normal, 2)).normalize(),
                    value(uv, 0),
                    value(uv, 1));
        }
        MeshUvHandedness.requireVanilla(
                GEOMETRY,
                vertices[0].position(), vertices[0].u(), vertices[0].v(),
                vertices[1].position(), vertices[1].u(), vertices[1].v(),
                vertices[2].position(), vertices[2].u(), vertices[2].v());
        Vector3f faceNormal = new Vector3f(vertices[1].position()).sub(vertices[0].position())
                .cross(new Vector3f(vertices[2].position()).sub(vertices[0].position()))
                .normalize();
        return new MeshQuad(
                vertices,
                Direction.getApproximateNearest(faceNormal.x, faceNormal.y, faceNormal.z));
    }

    private static float value(JsonArray array, int index) {
        return array.get(index).getAsFloat();
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
        this.mesh.outputTo(emitter);
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
        this.mesh.outputTo(emitter);
    }

    private record MeshQuad(MeshVertex[] vertices, Direction nominalFace) {
    }

    private record MeshVertex(Vector3f position, Vector3f normal, float u, float v) {
    }
}
