package com.recrivenvi.ravensmod.platform;

import com.recrivenvi.ravensmod.register.BlockEntities;
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
//? fabric {
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
//?}

import java.util.Set;
import java.util.function.Supplier;

public class PlatformRegistries {
    //? >=26.1 {
    public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(Identifier id, BlockEntities.EntityFactory<T> factory, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, createBlockEntityType(factory, blocks));
    }
    //?} else {
    /*public static <T extends BlockEntity> BlockEntityType<T> registerBlockEntityType(ResourceLocation id, BlockEntities.EntityFactory<T> factory, Block... blocks) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, createBlockEntityType(factory, blocks));
    }*/
    //?}

    public static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(BlockEntities.EntityFactory<T> factory, Block... blocks) {
        //? fabric {
        return FabricBlockEntityTypeBuilder.<T>create(factory::create, blocks).build();
        //?} else {
        /*return new BlockEntityType<>(factory::create, Set.of(blocks));*/
        //?}
    }

    public static CreativeModeTab createCreativeTab(Supplier<ItemStack> icon, Component title) {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0).icon(icon).title(title).build();
    }
}
