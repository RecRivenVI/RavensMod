package io.github.recrivenvi.ravensmod.registry;

import io.github.recrivenvi.ravensmod.RavensMod;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTabs {
    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "item_group"));

    public static final CreativeModeTab CUSTOM_ITEM_GROUP = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .icon(() -> new ItemStack(ModItems.RECRIVEN_VI))
            .title(Component.translatable("itemGroup.ravensmod"))
            .build();
}
