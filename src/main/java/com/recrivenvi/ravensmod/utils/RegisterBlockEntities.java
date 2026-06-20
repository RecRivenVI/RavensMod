package com.recrivenvi.ravensmod.utils;

import com.recrivenvi.ravensmod.RavensMod;
import com.recrivenvi.ravensmod.platform.PlatformRegistries;
import com.recrivenvi.ravensmod.register.BlockEntity1Register;
import com.recrivenvi.ravensmod.register.BlockEntity2Register;
import com.recrivenvi.ravensmod.register.BlockEntity3Register;
import com.recrivenvi.ravensmod.register.BlockEntity4Register;
import com.recrivenvi.ravensmod.register.BlockEntity5Register;
import com.recrivenvi.ravensmod.register.BlocksX;
import com.recrivenvi.ravensmod.register.BlockEntities;
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
//? neoforge {
/*import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;*/
//?}

public class RegisterBlockEntities {
    //? neoforge {
    /*public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RavensMod.MOD_ID);

    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String name,
            BlockEntities.EntityFactory<T> entityFactory,
            java.util.function.Supplier<? extends Block> block) {
        return BLOCK_ENTITY_TYPES.register(name, () -> PlatformRegistries.createBlockEntityType(entityFactory, block.get()));
    }*/
    //?} else {
    public static <T extends BlockEntity> BlockEntityType<T> register(String name,
            BlockEntities.EntityFactory<T> entityFactory,
            Block... blocks) {
        //? >=26.1 {
        Identifier id = Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name);
        //?} else {
        /*ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);*/
        //?}
        return PlatformRegistries.registerBlockEntityType(id, entityFactory, blocks);
    }
    //?}

    public static void initialize() {

    }

    //? neoforge {
    /*public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity1Register>> BLOCK_ENTITY_1 = register("block_entity_1", BlockEntity1Register::new, BlocksX.BLOCK_ENTITY_1);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity2Register>> BLOCK_ENTITY_2 = register("block_entity_2", BlockEntity2Register::new, BlocksX.BLOCK_ENTITY_2);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity3Register>> BLOCK_ENTITY_3 = register("block_entity_3", BlockEntity3Register::new, BlocksX.BLOCK_ENTITY_3);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity4Register>> BLOCK_ENTITY_4 = register("block_entity_4", BlockEntity4Register::new, BlocksX.BLOCK_ENTITY_4);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntity5Register>> BLOCK_ENTITY_5 = register("block_entity_5", BlockEntity5Register::new, BlocksX.BLOCK_ENTITY_5);*/
    //?} else {
    public static final BlockEntityType<BlockEntity1Register> BLOCK_ENTITY_1 = register("block_entity_1", BlockEntity1Register::new, BlocksX.BLOCK_ENTITY_1);
    public static final BlockEntityType<BlockEntity2Register> BLOCK_ENTITY_2 = register("block_entity_2", BlockEntity2Register::new, BlocksX.BLOCK_ENTITY_2);
    public static final BlockEntityType<BlockEntity3Register> BLOCK_ENTITY_3 = register("block_entity_3", BlockEntity3Register::new, BlocksX.BLOCK_ENTITY_3);
    public static final BlockEntityType<BlockEntity4Register> BLOCK_ENTITY_4 = register("block_entity_4", BlockEntity4Register::new, BlocksX.BLOCK_ENTITY_4);
    public static final BlockEntityType<BlockEntity5Register> BLOCK_ENTITY_5 = register("block_entity_5", BlockEntity5Register::new, BlocksX.BLOCK_ENTITY_5);
    //?}
}
