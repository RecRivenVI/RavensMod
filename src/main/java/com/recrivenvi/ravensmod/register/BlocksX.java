package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.RavensMod;
import com.recrivenvi.ravensmod.compat.CompatBlockItem;
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
//? neoforge {
/*import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;*/
//?}

import java.util.function.Function;


public class BlocksX {
    //? neoforge {
    /*public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RavensMod.MOD_ID);

    public static <T extends Block> DeferredBlock<T> register(String name, Function<BlockBehaviour.Properties, T> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        //? >=26.1 {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, blockFactory, () -> settings);
        //?} else {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, blockFactory, settings);
        //?}
        if (shouldRegisterItem) {
            ItemsX.ITEMS.registerItem(name, properties -> new CompatBlockItem(block.get(), properties));
        }
        return block;
    }*/
    //?} else {
    public static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        //? >=26.1 {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.setId(blockKey));
        if (shouldRegisterItem) {
            ResourceKey<Item> itemKey = keyOfItem(name);
            CompatBlockItem blockItem = new CompatBlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        //?} else {
        /*ResourceLocation id = keyOfBlock(name);
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        Block block = blockFactory.apply(settings.setId(blockKey));
        if (shouldRegisterItem) {
            ResourceLocation itemId = keyOfItem(name);
            ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, itemId);
            CompatBlockItem blockItem = new CompatBlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemId, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, id, block);*/
        //?}
    }
    //?}

    //? >=26.1 {
    public static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }

    public static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }
    //?} else {
    /*public static ResourceLocation keyOfBlock(String name) {
        return ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
    }

    public static ResourceLocation keyOfItem(String name) {
        return ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
    }*/
    //?}

    //? neoforge {
    /*public static final DeferredBlock<Block> MIRROR_BLOCK = register("mirror_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.GLASS), true);
    public static final DeferredBlock<Block> WHITE_BLOCK = register("white_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);
    public static final DeferredBlock<BlockEntities<BlockEntity1Register>> BLOCK_ENTITY_1 = register("block_entity_1", settings -> new BlockEntities<>(settings, BlockEntity1Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final DeferredBlock<BlockEntities<BlockEntity2Register>> BLOCK_ENTITY_2 = register("block_entity_2", settings -> new BlockEntities<>(settings, BlockEntity2Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final DeferredBlock<BlockEntities<BlockEntity3Register>> BLOCK_ENTITY_3 = register("block_entity_3", settings -> new BlockEntities<>(settings, BlockEntity3Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final DeferredBlock<VerticalBlockEntities<BlockEntity4Register>> BLOCK_ENTITY_4 = register("block_entity_4", settings -> new VerticalBlockEntities<>(settings, BlockEntity4Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final DeferredBlock<BlockEntities<BlockEntity5Register>> BLOCK_ENTITY_5 = register("block_entity_5", settings -> new BlockEntities<>(settings, BlockEntity5Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);*/
    //?} else {
    public static final Block MIRROR_BLOCK = register("mirror_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.GLASS), true);
    public static final Block WHITE_BLOCK = register("white_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);
    public static final Block BLOCK_ENTITY_1 = register("block_entity_1", settings -> new BlockEntities<>(settings, BlockEntity1Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_2 = register("block_entity_2", settings -> new BlockEntities<>(settings, BlockEntity2Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_3 = register("block_entity_3", settings -> new BlockEntities<>(settings, BlockEntity3Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_4 = register("block_entity_4", settings -> new VerticalBlockEntities<>(settings, BlockEntity4Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_5 = register("block_entity_5", settings -> new BlockEntities<>(settings, BlockEntity5Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    //?}

    public static void initialize() {
    }
}
