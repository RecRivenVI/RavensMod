package io.github.recrivenvi.ravensmodels;

import io.github.recrivenvi.ravensmodels.block.DirectionalModelBlock;
import io.github.recrivenvi.ravensmodels.block.HorizontalModelBlock;
import io.github.recrivenvi.ravensmodels.block.ModelBlockEntity;
import io.github.recrivenvi.ravensmodels.block.ModelBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModContent {
    public static final Block MIRROR_BLOCK = registerSimpleBlock("mirror_block", SoundType.GLASS);
    public static final Block WHITE_BLOCK = registerSimpleBlock("white_block", SoundType.STONE);
    public static final HorizontalModelBlock SHATTERED_THRONE = registerModelBlock(
            "shattered_throne", HorizontalModelBlock::new, SoundType.STONE);
    public static final HorizontalModelBlock LOWER_ROUND_CORNER = registerModelBlock(
            "lower_round_corner", HorizontalModelBlock::new, SoundType.STONE);
    public static final HorizontalModelBlock UPPER_ROUND_CORNER = registerModelBlock(
            "upper_round_corner", HorizontalModelBlock::new, SoundType.STONE);
    public static final DirectionalModelBlock FLAT_WALL = registerModelBlock(
            "flat_wall", DirectionalModelBlock::new, SoundType.STONE);
    public static final HorizontalModelBlock VERTICAL_WALL = registerModelBlock(
            "vertical_wall", HorizontalModelBlock::new, SoundType.STONE);

    public static final BlockEntityType<ModelBlockEntity> MODEL_BLOCK_ENTITY_TYPE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            id("model_block"),
            FabricBlockEntityTypeBuilder.create(
                    ModelBlockEntity::new,
                    SHATTERED_THRONE,
                    LOWER_ROUND_CORNER,
                    UPPER_ROUND_CORNER,
                    FLAT_WALL,
                    VERTICAL_WALL).build());

    private static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), id("ravensmodels"));
    private static final CreativeModeTab CREATIVE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(SHATTERED_THRONE))
            .title(Component.translatable("itemGroup.ravensmodels"))
            .build();

    private ModContent() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.accept(MIRROR_BLOCK);
            entries.accept(WHITE_BLOCK);
            entries.accept(SHATTERED_THRONE);
            entries.accept(LOWER_ROUND_CORNER);
            entries.accept(UPPER_ROUND_CORNER);
            entries.accept(FLAT_WALL);
            entries.accept(VERTICAL_WALL);
        });
    }

    private static Block registerSimpleBlock(String name, SoundType sound) {
        return registerBlock(name, Block::new, sound, false);
    }

    private static <T extends Block> T registerModelBlock(
            String name, Function<BlockBehaviour.Properties, T> factory, SoundType sound) {
        return registerBlock(name, factory, sound, true);
    }

    private static <T extends Block> T registerBlock(
            String name, Function<BlockBehaviour.Properties, T> factory, SoundType sound, boolean modelBlock) {
        ResourceLocation id = id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().sound(sound).setId(blockKey);
        if (modelBlock) {
            properties.noOcclusion();
        }

        T block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties().setId(itemKey);
        BlockItem item = modelBlock
                ? new ModelBlockItem(block, itemProperties)
                : new BlockItem(block, itemProperties);
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return block;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }
}
