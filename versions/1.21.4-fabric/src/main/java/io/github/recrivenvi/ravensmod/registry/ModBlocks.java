package io.github.recrivenvi.ravensmod.registry;

import io.github.recrivenvi.ravensmod.RavensMod;
import io.github.recrivenvi.ravensmod.block.HorizontalEntityBlock;
import io.github.recrivenvi.ravensmod.block.VerticalEntityBlock;
import io.github.recrivenvi.ravensmod.block.entity.FlatWallBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.RoundCornerOneBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.RoundCornerTwoBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.ShatteredThroneBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.VerticalWallBlockEntity;
import io.github.recrivenvi.ravensmod.item.AnimatedBlockItem;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;


public class ModBlocks {
    public static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        ResourceLocation id = keyOfBlock(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = blockFactory.apply(settings.setId(blockKey));
        if (shouldRegisterItem) {
            ResourceLocation itemId = keyOfItem(name);
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
            AnimatedBlockItem blockItem = new AnimatedBlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemId, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static ResourceLocation keyOfBlock(String name) {
        return ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
    }

    public static ResourceLocation keyOfItem(String name) {
        return ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
    }

    public static final Block MIRROR_BLOCK = register("mirror_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.GLASS), true);
    public static final Block WHITE_BLOCK = register("white_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);
    public static final Block BLOCK_ENTITY_1 = register("block_entity_1", settings -> new HorizontalEntityBlock<>(settings, ShatteredThroneBlockEntity::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_2 = register("block_entity_2", settings -> new HorizontalEntityBlock<>(settings, RoundCornerOneBlockEntity::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_3 = register("block_entity_3", settings -> new HorizontalEntityBlock<>(settings, RoundCornerTwoBlockEntity::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_4 = register("block_entity_4", settings -> new VerticalEntityBlock<>(settings, FlatWallBlockEntity::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_5 = register("block_entity_5", settings -> new HorizontalEntityBlock<>(settings, VerticalWallBlockEntity::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);

    public static void initialize() {
    }
}
