package io.github.recrivenvi.ravensmodels.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.model.ElementsModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

public final class ModelGeometryLoader
        implements IGeometryLoader<ModelGeometryLoader.Geometry>, ResourceManagerReloadListener {
    private final Map<String, Geometry> geometries = new ConcurrentHashMap<>();
    private final ResourceManager resources = Minecraft.getInstance().getResourceManager();

    @Override
    public Geometry read(JsonObject json, JsonDeserializationContext context) {
        if (!json.has("mesh")) {
            throw new JsonParseException("Raven's Models geometry requires a mesh name");
        }
        return this.geometries.computeIfAbsent(
                json.get("mesh").getAsString(), name -> new Geometry(name, this.resources));
    }

    @Override
    public void onResourceManagerReload(ResourceManager resources) {
        this.geometries.clear();
    }

    public static final class Geometry implements IUnbakedGeometry<Geometry> {
        private final String name;
        private final ResourceManager resources;
        private Function<BakedModel, BakedModel> prepared;

        private Geometry(String name, ResourceManager resources) {
            this.name = name;
            this.resources = resources;
        }

        @Override
        public synchronized BakedModel bake(
                IGeometryBakingContext context,
                ModelBaker baker,
                Function<Material, TextureAtlasSprite> sprites,
                ModelState modelState,
                ItemOverrides overrides) {
            if (this.prepared == null) {
                this.prepared =
                        switch (this.name) {
                            case "mirror_sphere", "white_sphere" ->
                                    SphereBakedModel.prepare(this.resources, sprites, this.name);
                            case "dodecahedron" ->
                                    DodecahedronBakedModel.prepare(
                                            this.resources, sprites, this.name);
                            case "g3_round_corner" ->
                                    G3CornerBakedModel.prepare(this.resources, sprites, this.name);
                            case "shattered_throne" ->
                                    ThroneBakedModel.prepare(this.resources, sprites, this.name);
                            case "david_bust", "plastic_chair" ->
                                    DecorativeMeshBakedModel.prepare(
                                            this.resources, sprites, this.name);
                            case "ace_of_spades" ->
                                    AceOfSpadesBakedModel.prepare(
                                            this.resources, sprites, this.name);
                            default -> throw new JsonParseException("Unknown mesh: " + this.name);
                        };
            }
            // Geometry owns state rotations; the context still owns item display and render layers.
            BakedModel display =
                    new ElementsModel(List.of())
                            .bake(context, baker, sprites, modelState, overrides);
            return this.prepared.apply(display);
        }
    }
}
