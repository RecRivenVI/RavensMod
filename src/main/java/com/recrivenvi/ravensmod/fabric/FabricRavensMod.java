package com.recrivenvi.ravensmod.fabric;

import com.recrivenvi.ravensmod.RavensMod;
import com.recrivenvi.ravensmod.register.BlocksX;
import com.recrivenvi.ravensmod.register.ItemsX;
import com.recrivenvi.ravensmod.register.TabsX;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.fabricmc.api.ModInitializer;
//? >=26.1 {
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
//?} else {
/*import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;*/
//?}

public class FabricRavensMod implements ModInitializer {
    @Override
    public void onInitialize() {
        BlocksX.initialize();
        RegisterBlockEntities.initialize();
        ItemsX.initialize();

        //? >=26.1 {
        CreativeModeTabEvents.modifyOutputEvent(TabsX.CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(BlocksX.MIRROR_BLOCK.asItem());
            itemGroup.accept(BlocksX.WHITE_BLOCK.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_1.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_2.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_3.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_4.asItem());
            itemGroup.accept(BlocksX.BLOCK_ENTITY_5.asItem());
            itemGroup.accept(ItemsX.RECRIVEN_VI);
        });
        //?} else {
        /*ItemGroupEvents.modifyEntriesEvent(TabsX.CUSTOM_ITEM_GROUP_KEY).register(entries -> {
            entries.accept(BlocksX.MIRROR_BLOCK.asItem());
            entries.accept(BlocksX.WHITE_BLOCK.asItem());
            entries.accept(BlocksX.BLOCK_ENTITY_1.asItem());
            entries.accept(BlocksX.BLOCK_ENTITY_2.asItem());
            entries.accept(BlocksX.BLOCK_ENTITY_3.asItem());
            entries.accept(BlocksX.BLOCK_ENTITY_4.asItem());
            entries.accept(BlocksX.BLOCK_ENTITY_5.asItem());
            entries.accept(ItemsX.RECRIVEN_VI);
        });*/
        //?}

        RavensMod.LOGGER.info("A BIG HELLO FROM RECTECH!!!");
    }
}
