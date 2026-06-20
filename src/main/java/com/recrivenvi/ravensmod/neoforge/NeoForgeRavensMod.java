package com.recrivenvi.ravensmod.neoforge;

import com.recrivenvi.ravensmod.RavensMod;
import com.recrivenvi.ravensmod.register.BlocksX;
import com.recrivenvi.ravensmod.register.ItemsX;
import com.recrivenvi.ravensmod.register.TabsX;
import com.recrivenvi.ravensmod.utils.RegisterBlockEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(RavensMod.MOD_ID)
public class NeoForgeRavensMod {
    public NeoForgeRavensMod(IEventBus modEventBus) {
        BlocksX.BLOCKS.register(modEventBus);
        ItemsX.ITEMS.register(modEventBus);
        RegisterBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        TabsX.CREATIVE_TABS.register(modEventBus);

        BlocksX.initialize();
        RegisterBlockEntities.initialize();
        ItemsX.initialize();

        modEventBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey().equals(TabsX.CUSTOM_ITEM_GROUP_KEY)) {
                event.accept(BlocksX.MIRROR_BLOCK.asItem());
                event.accept(BlocksX.WHITE_BLOCK.asItem());
                event.accept(BlocksX.BLOCK_ENTITY_1.asItem());
                event.accept(BlocksX.BLOCK_ENTITY_2.asItem());
                event.accept(BlocksX.BLOCK_ENTITY_3.asItem());
                event.accept(BlocksX.BLOCK_ENTITY_4.asItem());
                event.accept(BlocksX.BLOCK_ENTITY_5.asItem());
                event.accept(ItemsX.RECRIVEN_VI);
            }
        });

        RavensMod.LOGGER.info("A BIG HELLO FROM RECTECH!!!");
    }
}
