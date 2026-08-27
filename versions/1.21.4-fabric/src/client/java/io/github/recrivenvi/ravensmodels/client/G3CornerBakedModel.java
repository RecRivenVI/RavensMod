package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.recrivenvi.ravensmodels.ModContent;
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
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Reload-safe G3 meshes pre-baked for every stair-like block state. */
public final class G3CornerBakedModel implements BakedModel, FabricBakedModel {
    private static final ResourceLocation STRAIGHT_GEOMETRY = id("models/mesh/g3/straight.json");
    private static final ResourceLocation INNER_GEOMETRY = id("models/mesh/g3/inner.json");
    private static final ResourceLocation OUTER_GEOMETRY = id("models/mesh/g3/outer.json");
    private static final ResourceLocation BLOCK = id("g3_round_corner");
    private static final ResourceLocation MODEL = id("block/g3_round_corner");
    private static final ResourceLocation STRAIGHT_TEXTURE = id("block/g3_straight_atlas");
    private static final ResourceLocation INNER_TEXTURE = id("block/g3_inner_atlas");
    private static final ResourceLocation OUTER_TEXTURE = id("block/g3_outer_atlas");

    private final BakedModel delegate;
    private final Map<BlockState, Mesh> stateMeshes;
    private final Mesh itemMesh;

    private G3CornerBakedModel(BakedModel delegate, Map<BlockState, Mesh> stateMeshes) {
        this.delegate = delegate;
        this.stateMeshes = stateMeshes;
        this.itemMesh = stateMeshes.get(ModContent.G3_ROUND_CORNER.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.HALF, Half.BOTTOM)
                .setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT)
                .setValue(BlockStateProperties.WATERLOGGED, false));
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

    private static BakedModel bake(BakedModel model, ModelBaker baker, Geometry geometry) {
        if (model instanceof G3CornerBakedModel) {
            return model;
        }

        Renderer renderer = Renderer.get();
        RenderMaterial material = renderer.materialFinder().blendMode(BlendMode.SOLID).find();
        Map<BlockState, Mesh> meshes = new HashMap<>();
        for (BlockState state : ModContent.G3_ROUND_CORNER.getStateDefinition().getPossibleStates()) {
            MeshPart part = geometry.forShape(state.getValue(BlockStateProperties.STAIRS_SHAPE));
            TextureAtlasSprite sprite = baker.sprites().get(
                    new Material(TextureAtlas.LOCATION_BLOCKS, part.texture()));
            meshes.put(state, buildMesh(
                    renderer,
                    material,
                    sprite,
                    part.quads(),
                    StateTransform.forState(state)));
        }
        return new G3CornerBakedModel(model, Map.copyOf(meshes));
    }

    private static Mesh buildMesh(
            Renderer renderer,
            RenderMaterial material,
            TextureAtlasSprite sprite,
            List<MeshQuad> geometry,
            StateTransform transform) {
        MutableMesh mutableMesh = renderer.mutableMesh();
        QuadEmitter emitter = mutableMesh.emitter();
        for (MeshQuad quad : geometry) {
            Vector3f[] transformedPositions = new Vector3f[4];
            for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                MeshVertex vertex = quad.vertices()[vertexIndex];
                Vector3f position = transform.position(vertex.position());
                Vector3f normal = transform.normal(vertex.normal());
                transformedPositions[vertexIndex] = position;
                emitter.pos(vertexIndex, position.x, position.y, position.z);
                emitter.uv(vertexIndex, vertex.u(), vertex.v());
                emitter.normal(vertexIndex, normal.x, normal.y, normal.z);
                emitter.color(vertexIndex, 0xFFFFFFFF);
            }

            Vector3f faceNormal = new Vector3f(transformedPositions[1]).sub(transformedPositions[0])
                    .cross(new Vector3f(transformedPositions[2]).sub(transformedPositions[0]))
                    .normalize();
            emitter
                    .spriteBake(sprite, MutableQuadView.BAKE_NORMALIZED)
                    .material(material)
                    .nominalFace(Direction.getApproximateNearest(faceNormal.x, faceNormal.y, faceNormal.z))
                    .cullFace(null)
                    .emit();
        }
        return mutableMesh.immutableCopy();
    }

    private static Geometry loadGeometry(ResourceManager resourceManager) {
        return new Geometry(
                loadMesh(resourceManager, STRAIGHT_GEOMETRY),
                loadMesh(resourceManager, INNER_GEOMETRY),
                loadMesh(resourceManager, OUTER_GEOMETRY));
    }

    private static List<MeshQuad> loadMesh(ResourceManager resourceManager, ResourceLocation geometry) {
        try {
            Resource resource = resourceManager.getResourceOrThrow(geometry);
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.get("uv_handedness").getAsInt() != -1) {
                    throw new IllegalStateException("G3 mesh must declare uv_handedness=-1: " + geometry);
                }
                float textureWidth = root.get("texture_width").getAsFloat();
                float textureHeight = root.get("texture_height").getAsFloat();
                JsonArray rawVertices = root.getAsJsonArray("vertices");
                Vector3f[] positions = new Vector3f[rawVertices.size()];
                for (int index = 0; index < rawVertices.size(); index++) {
                    JsonArray vertex = rawVertices.get(index).getAsJsonArray();
                    positions[index] = new Vector3f(value(vertex, 0), value(vertex, 1), value(vertex, 2));
                }

                JsonArray rawQuads = root.getAsJsonArray("quads");
                List<MeshQuad> quads = new ArrayList<>(rawQuads.size());
                for (JsonElement element : rawQuads) {
                    quads.add(readQuad(
                            element.getAsJsonObject(),
                            positions,
                            geometry,
                            textureWidth,
                            textureHeight));
                }
                return List.copyOf(quads);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load G3 geometry " + geometry, exception);
        }
    }

    private static MeshQuad readQuad(
            JsonObject quad,
            Vector3f[] positions,
            ResourceLocation geometry,
            float textureWidth,
            float textureHeight) {
        JsonArray indices = quad.getAsJsonArray("vertices");
        JsonArray rawUvs = quad.getAsJsonArray("uv");
        JsonArray rawNormals = quad.getAsJsonArray("normals");
        MeshVertex[] vertices = new MeshVertex[4];
        for (int index = 0; index < 4; index++) {
            Vector3f position = positions[indices.get(index).getAsInt()];
            JsonArray uv = rawUvs.get(index).getAsJsonArray();
            JsonArray normal = rawNormals.get(index).getAsJsonArray();
            vertices[index] = new MeshVertex(
                    new Vector3f(position),
                    new Vector3f(value(normal, 0), value(normal, 1), value(normal, 2)).normalize(),
                    value(uv, 0) / textureWidth,
                    value(uv, 1) / textureHeight);
        }
        MeshUvHandedness.requireVanilla(
                geometry,
                vertices[0].position(), vertices[0].u(), vertices[0].v(),
                vertices[1].position(), vertices[1].u(), vertices[1].v(),
                vertices[2].position(), vertices[2].u(), vertices[2].v());
        return new MeshQuad(vertices);
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
        this.stateMeshes.getOrDefault(state, this.itemMesh).outputTo(emitter);
    }

    @Override
    public void emitItemQuads(QuadEmitter emitter, Supplier<RandomSource> randomSupplier) {
        this.itemMesh.outputTo(emitter);
    }

    private record Geometry(List<MeshQuad> straight, List<MeshQuad> inner, List<MeshQuad> outer) {
        MeshPart forShape(StairsShape shape) {
            return switch (shape) {
                case INNER_LEFT, INNER_RIGHT -> new MeshPart(this.inner, INNER_TEXTURE);
                case OUTER_LEFT, OUTER_RIGHT -> new MeshPart(this.outer, OUTER_TEXTURE);
                default -> new MeshPart(this.straight, STRAIGHT_TEXTURE);
            };
        }
    }

    private record MeshPart(List<MeshQuad> quads, ResourceLocation texture) {
    }

    private record MeshQuad(MeshVertex[] vertices) {
    }

    private record MeshVertex(Vector3f position, Vector3f normal, float u, float v) {
    }

    private record StateTransform(int yQuarterTurns, @Nullable Direction topRotationAxis) {
        static StateTransform forState(BlockState state) {
            StairsShape targetShape = state.getValue(BlockStateProperties.STAIRS_SHAPE);
            boolean top = state.getValue(BlockStateProperties.HALF) == Half.TOP;
            StairsShape sourceShape = top ? swapLeftRight(targetShape) : targetShape;
            int facingTurns = switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case EAST -> 1;
                case SOUTH -> 2;
                case WEST -> 3;
                default -> 0;
            };
            if (sourceShape == StairsShape.INNER_LEFT || sourceShape == StairsShape.OUTER_LEFT) {
                facingTurns = Math.floorMod(facingTurns - 1, 4);
            }
            return new StateTransform(
                    facingTurns,
                    top ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : null);
        }

        Vector3f position(Vector3f source) {
            Vector3f result = rotateY(new Vector3f(source), this.yQuarterTurns);
            if (this.topRotationAxis != null) {
                rotateTop(result, this.topRotationAxis, true);
            }
            return result;
        }

        Vector3f normal(Vector3f source) {
            Vector3f result = rotateYDirection(new Vector3f(source), this.yQuarterTurns);
            if (this.topRotationAxis != null) {
                rotateTop(result, this.topRotationAxis, false);
            }
            return result.normalize();
        }

        private static Vector3f rotateY(Vector3f vector, int turns) {
            vector.sub(0.5f, 0.5f, 0.5f);
            rotateYDirection(vector, turns);
            return vector.add(0.5f, 0.5f, 0.5f);
        }

        private static Vector3f rotateYDirection(Vector3f vector, int turns) {
            for (int index = 0; index < turns; index++) {
                float x = vector.x;
                vector.x = -vector.z;
                vector.z = x;
            }
            return vector;
        }

        private static void rotateTop(Vector3f vector, Direction axis, boolean position) {
            if (position) {
                vector.sub(0.5f, 0.5f, 0.5f);
            }
            if (axis.getAxis() == Direction.Axis.X) {
                vector.y = -vector.y;
                vector.z = -vector.z;
            } else {
                vector.x = -vector.x;
                vector.y = -vector.y;
            }
            if (position) {
                vector.add(0.5f, 0.5f, 0.5f);
            }
        }

        private static StairsShape swapLeftRight(StairsShape shape) {
            return switch (shape) {
                case INNER_LEFT -> StairsShape.INNER_RIGHT;
                case INNER_RIGHT -> StairsShape.INNER_LEFT;
                case OUTER_LEFT -> StairsShape.OUTER_RIGHT;
                case OUTER_RIGHT -> StairsShape.OUTER_LEFT;
                default -> StairsShape.STRAIGHT;
            };
        }
    }
}
