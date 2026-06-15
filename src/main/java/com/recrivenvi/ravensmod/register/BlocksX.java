package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.RavensMod;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;


public class BlocksX {
    public static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.setId(blockKey));
        if (shouldRegisterItem) {
            ResourceKey<Item> itemKey = keyOfItem(name);
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
    }

    public static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }

    public static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }

    public static final Block MIRROR_BLOCK = register("mirror_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.GLASS), true);
    public static final Block WHITE_BLOCK = register("white_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);
    public static final Block BLOCK_ENTITY_1 = register("block_entity_1", settings -> new BlockEntities<>(settings, BlockEntity1Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_2 = register("block_entity_2", settings -> new BlockEntities<>(settings, BlockEntity2Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_3 = register("block_entity_3", settings -> new BlockEntities<>(settings, BlockEntity3Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_4 = register("block_entity_4", settings -> new BlockEntities<>(settings, BlockEntity4Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_5 = register("block_entity_5", settings -> new BlockEntities<>(settings, BlockEntity5Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(TabsX.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(BlocksX.MIRROR_BLOCK.asItem());
            itemGroup.accept(BlocksX.WHITE_BLOCK.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_1.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_2.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_3.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_4.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_5.asItem());
        });
    }
}
