package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.RavensMod;
//? >=26.1 {
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;
//?} else {
/*import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;*/
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

import java.util.function.Function;


public class BlocksX {
    public static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties settings, boolean shouldRegisterItem) {
        //? >=26.1 {
        ResourceKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.setId(blockKey));
        if (shouldRegisterItem) {
            ResourceKey<Item> itemKey = keyOfItem(name);
            BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        //?} else {
        /*ResourceLocation id = keyOfBlock(name);
        Block block = blockFactory.apply(settings);
        if (shouldRegisterItem) {
            ResourceLocation itemId = keyOfItem(name);
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, itemId, blockItem);
        }
        return Registry.register(BuiltInRegistries.BLOCK, id, block);*/
        //?}
    }

    //? >=26.1 {
    public static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }

    public static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
    }
    //?} else {
    /*public static ResourceLocation keyOfBlock(String name) {
        return new ResourceLocation(RavensMod.MOD_ID, name);
    }

    public static ResourceLocation keyOfItem(String name) {
        return new ResourceLocation(RavensMod.MOD_ID, name);
    }*/
    //?}

    public static final Block MIRROR_BLOCK = register("mirror_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.GLASS), true);
    public static final Block WHITE_BLOCK = register("white_block", Block::new, BlockBehaviour.Properties.of().sound(SoundType.STONE), true);
    public static final Block BLOCK_ENTITY_1 = register("block_entity_1", settings -> new BlockEntities<>(settings, BlockEntity1Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_2 = register("block_entity_2", settings -> new BlockEntities<>(settings, BlockEntity2Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_3 = register("block_entity_3", settings -> new BlockEntities<>(settings, BlockEntity3Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_4 = register("block_entity_4", settings -> new BlockEntities<>(settings, BlockEntity4Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);
    public static final Block BLOCK_ENTITY_5 = register("block_entity_5", settings -> new BlockEntities<>(settings, BlockEntity5Register::new), BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion(), true);

    public static void initialize() {
        //? >=26.1 {
        CreativeModeTabEvents.modifyOutputEvent(TabsX.CUSTOM_ITEM_GROUP_KEY).register((itemGroup) -> {
            itemGroup.accept(BlocksX.MIRROR_BLOCK.asItem());
            itemGroup.accept(BlocksX.WHITE_BLOCK.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_1.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_2.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_3.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_4.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_5.asItem());
        });
        //?} else {
        /*ItemGroupEvents.modifyEntriesEvent(TabsX.CUSTOM_ITEM_GROUP_KEY).register(entries -> {
            entries.add(BlocksX.MIRROR_BLOCK.asItem());
            entries.add(BlocksX.WHITE_BLOCK.asItem());
            entries.add(BlocksX.BLOCK_ENTITY_1.asItem());
            entries.add(BlocksX.BLOCK_ENTITY_2.asItem());
            entries.add(BlocksX.BLOCK_ENTITY_3.asItem());
            entries.add(BlocksX.BLOCK_ENTITY_4.asItem());
            entries.add(BlocksX.BLOCK_ENTITY_5.asItem());
        });*/
        //?}
    }
}
