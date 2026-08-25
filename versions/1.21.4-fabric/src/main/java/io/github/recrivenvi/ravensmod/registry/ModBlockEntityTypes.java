package io.github.recrivenvi.ravensmod.registry;

import io.github.recrivenvi.ravensmod.RavensMod;
import io.github.recrivenvi.ravensmod.block.entity.ShatteredThroneBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.RoundCornerOneBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.RoundCornerTwoBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.FlatWallBlockEntity;
import io.github.recrivenvi.ravensmod.block.entity.VerticalWallBlockEntity;
import io.github.recrivenvi.ravensmod.block.HorizontalEntityBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntityTypes {
    public static <T extends BlockEntity> BlockEntityType<T> register(String name,
            HorizontalEntityBlock.EntityFactory<T> entityFactory,
            Block... blocks) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
        BlockEntityType<T> type = FabricBlockEntityTypeBuilder.<T>create(entityFactory::create, blocks).build();
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type);
    }

    public static void initialize() {

    }

    public static final BlockEntityType<ShatteredThroneBlockEntity> BLOCK_ENTITY_1 = register("block_entity_1", ShatteredThroneBlockEntity::new, ModBlocks.BLOCK_ENTITY_1);
    public static final BlockEntityType<RoundCornerOneBlockEntity> BLOCK_ENTITY_2 = register("block_entity_2", RoundCornerOneBlockEntity::new, ModBlocks.BLOCK_ENTITY_2);
    public static final BlockEntityType<RoundCornerTwoBlockEntity> BLOCK_ENTITY_3 = register("block_entity_3", RoundCornerTwoBlockEntity::new, ModBlocks.BLOCK_ENTITY_3);
    public static final BlockEntityType<FlatWallBlockEntity> BLOCK_ENTITY_4 = register("block_entity_4", FlatWallBlockEntity::new, ModBlocks.BLOCK_ENTITY_4);
    public static final BlockEntityType<VerticalWallBlockEntity> BLOCK_ENTITY_5 = register("block_entity_5", VerticalWallBlockEntity::new, ModBlocks.BLOCK_ENTITY_5);
}
