package io.github.recrivenvi.ravensmodels;

import io.github.recrivenvi.ravensmodels.block.DirectionalPanelBlock;
import io.github.recrivenvi.ravensmodels.block.G3StairBlock;
import io.github.recrivenvi.ravensmodels.block.HorizontalFacingBlock;
import io.github.recrivenvi.ravensmodels.block.WaterloggedBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModContent {
    public static final Block MIRROR_BLOCK = registerSimpleBlock("mirror_block", SoundType.GLASS);
    public static final Block WHITE_BLOCK = registerSimpleBlock("white_block", SoundType.STONE);
    public static final Block MIRROR_SPHERE = registerStaticMeshBlock("mirror_sphere", SoundType.GLASS);
    public static final Block WHITE_SPHERE = registerStaticMeshBlock("white_sphere", SoundType.STONE);
    public static final Block DODECAHEDRON = registerStaticMeshBlock("dodecahedron", SoundType.STONE);
    public static final HorizontalFacingBlock SHATTERED_THRONE = registerBlock(
            "shattered_throne", HorizontalFacingBlock::new, SoundType.STONE, true);
    public static final G3StairBlock G3_ROUND_CORNER = registerBlock(
            "g3_round_corner", G3StairBlock::new, SoundType.STONE, true);
    public static final DirectionalPanelBlock G3_CONNECTOR = registerBlock(
            "g3_connector", DirectionalPanelBlock::new, SoundType.STONE, true);

    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), id("ravensmodels"));
    private static final CreativeModeTab CREATIVE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(SHATTERED_THRONE))
            .title(Component.translatable("itemGroup.ravensmodels"))
            .displayItems((parameters, output) -> {
                output.accept(MIRROR_BLOCK);
                output.accept(WHITE_BLOCK);
                output.accept(MIRROR_SPHERE);
                output.accept(WHITE_SPHERE);
                output.accept(DODECAHEDRON);
                output.accept(SHATTERED_THRONE);
                output.accept(G3_ROUND_CORNER);
                output.accept(G3_CONNECTOR);
            })
            .build();

    private ModContent() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
    }

    private static Block registerSimpleBlock(String name, SoundType sound) {
        return registerBlock(name, Block::new, sound, false);
    }

    private static Block registerStaticMeshBlock(String name, SoundType sound) {
        return registerBlock(name, WaterloggedBlock::new, sound, true);
    }

    private static <T extends Block> T registerBlock(
            String name,
            Function<BlockBehaviour.Properties, T> factory,
            SoundType sound,
            boolean noOcclusion) {
        ResourceLocation id = id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().sound(sound).setId(blockKey);
        if (noOcclusion) {
            properties.noOcclusion();
        }

        T block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties().setId(itemKey);
        Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, itemProperties));
        return block;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }
}
