package io.github.recrivenvi.ravensmod.registry;

import io.github.recrivenvi.ravensmod.RavensMod;
import io.github.recrivenvi.ravensmod.block.HorizontalModelBlock;
import io.github.recrivenvi.ravensmod.block.OmnidirectionalModelBlock;
import io.github.recrivenvi.ravensmod.block.entity.AnimatedBlockEntity;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class ModContent {
    public static final Block MIRROR_BLOCK = registerBlock("mirror_block", Block::new, SoundType.GLASS, false);
    public static final Block WHITE_BLOCK = registerBlock("white_block", Block::new, SoundType.STONE, false);
    public static final HorizontalModelBlock SHATTERED_THRONE = registerBlock(
            "block_entity_1", properties -> new HorizontalModelBlock(properties, AnimatedBlockEntity.ShatteredThrone::new), SoundType.STONE, true);
    public static final HorizontalModelBlock ROUND_CORNER_ONE = registerBlock(
            "block_entity_2", properties -> new HorizontalModelBlock(properties, AnimatedBlockEntity.RoundCornerOne::new), SoundType.STONE, true);
    public static final HorizontalModelBlock ROUND_CORNER_TWO = registerBlock(
            "block_entity_3", properties -> new HorizontalModelBlock(properties, AnimatedBlockEntity.RoundCornerTwo::new), SoundType.STONE, true);
    public static final OmnidirectionalModelBlock FLAT_WALL = registerBlock(
            "block_entity_4", properties -> new OmnidirectionalModelBlock(properties, AnimatedBlockEntity.FlatWall::new), SoundType.STONE, true);
    public static final HorizontalModelBlock VERTICAL_WALL = registerBlock(
            "block_entity_5", properties -> new HorizontalModelBlock(properties, AnimatedBlockEntity.VerticalWall::new), SoundType.STONE, true);

    public static final BlockEntityType<AnimatedBlockEntity.ShatteredThrone> SHATTERED_THRONE_ENTITY =
            registerBlockEntity("block_entity_1", AnimatedBlockEntity.ShatteredThrone::new, SHATTERED_THRONE);
    public static final BlockEntityType<AnimatedBlockEntity.RoundCornerOne> ROUND_CORNER_ONE_ENTITY =
            registerBlockEntity("block_entity_2", AnimatedBlockEntity.RoundCornerOne::new, ROUND_CORNER_ONE);
    public static final BlockEntityType<AnimatedBlockEntity.RoundCornerTwo> ROUND_CORNER_TWO_ENTITY =
            registerBlockEntity("block_entity_3", AnimatedBlockEntity.RoundCornerTwo::new, ROUND_CORNER_TWO);
    public static final BlockEntityType<AnimatedBlockEntity.FlatWall> FLAT_WALL_ENTITY =
            registerBlockEntity("block_entity_4", AnimatedBlockEntity.FlatWall::new, FLAT_WALL);
    public static final BlockEntityType<AnimatedBlockEntity.VerticalWall> VERTICAL_WALL_ENTITY =
            registerBlockEntity("block_entity_5", AnimatedBlockEntity.VerticalWall::new, VERTICAL_WALL);

    public static final Item RECRIVEN_VI = registerItem("recriven_vi", Item::new);

    public static final ResourceKey<CreativeModeTab> CREATIVE_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(), id("item_group"));
    public static final CreativeModeTab CREATIVE_TAB = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(RECRIVEN_VI))
            .title(Component.translatable("itemGroup.ravensmod"))
            .build();

    private ModContent() {
    }

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CREATIVE_TAB_KEY, CREATIVE_TAB);
        ItemGroupEvents.modifyEntriesEvent(CREATIVE_TAB_KEY).register(entries -> {
            entries.accept(MIRROR_BLOCK.asItem());
            entries.accept(WHITE_BLOCK.asItem());
            entries.accept(SHATTERED_THRONE.asItem());
            entries.accept(ROUND_CORNER_ONE.asItem());
            entries.accept(ROUND_CORNER_TWO.asItem());
            entries.accept(FLAT_WALL.asItem());
            entries.accept(VERTICAL_WALL.asItem());
            entries.accept(RECRIVEN_VI);
        });
    }

    private static <T extends Block> T registerBlock(
            String name, Function<BlockBehaviour.Properties, T> factory, SoundType sound, boolean modelBlock) {
        ResourceLocation id = id(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().sound(sound).setId(blockKey);
        if (modelBlock) {
            properties.noOcclusion();
        }

        T block = factory.apply(properties);
        Registry.register(BuiltInRegistries.BLOCK, id, block);

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item.Properties itemProperties = new Item.Properties().setId(itemKey);
        BlockItem item = modelBlock
                ? new AnimatedBlockItem(block, itemProperties)
                : new BlockItem(block, itemProperties);
        Registry.register(BuiltInRegistries.ITEM, id, item);

        return block;
    }

    private static Item registerItem(String name, Function<Item.Properties, Item> factory) {
        ResourceLocation id = id(name);
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        return Registry.register(BuiltInRegistries.ITEM, id, factory.apply(new Item.Properties().setId(key)));
    }

    private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
            String name, FabricBlockEntityTypeBuilder.Factory<T> factory, Block block) {
        return Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id(name),
                FabricBlockEntityTypeBuilder.create(factory, block).build());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, path);
    }
}
