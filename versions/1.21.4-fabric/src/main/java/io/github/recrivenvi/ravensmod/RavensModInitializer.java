package io.github.recrivenvi.ravensmod;

import io.github.recrivenvi.ravensmod.RavensMod;
import io.github.recrivenvi.ravensmod.registry.ModBlocks;
import io.github.recrivenvi.ravensmod.registry.ModItems;
import io.github.recrivenvi.ravensmod.registry.ModCreativeTabs;
import io.github.recrivenvi.ravensmod.registry.ModBlockEntityTypes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class RavensModInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        ModBlocks.initialize();
        ModBlockEntityTypes.initialize();
        ModItems.initialize();

        ItemGroupEvents.modifyEntriesEvent(ModCreativeTabs.CUSTOM_ITEM_GROUP_KEY).register(entries -> {
            entries.accept(ModBlocks.MIRROR_BLOCK.asItem());
            entries.accept(ModBlocks.WHITE_BLOCK.asItem());
            entries.accept(ModBlocks.BLOCK_ENTITY_1.asItem());
            entries.accept(ModBlocks.BLOCK_ENTITY_2.asItem());
            entries.accept(ModBlocks.BLOCK_ENTITY_3.asItem());
            entries.accept(ModBlocks.BLOCK_ENTITY_4.asItem());
            entries.accept(ModBlocks.BLOCK_ENTITY_5.asItem());
            entries.accept(ModItems.RECRIVEN_VI);
        });

        RavensMod.LOGGER.info("A BIG HELLO FROM RECTECH!!!");
    }
}
