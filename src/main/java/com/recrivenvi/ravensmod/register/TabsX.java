package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.RavensMod;
//? >=26.1 {
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.resources.Identifier;
//?} else {
/*import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.resources.ResourceLocation;*/
//?}

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabsX {
    //? >=26.1 {
    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, "item_group"));
    public static final CreativeModeTab CUSTOM_ITEM_GROUP = FabricCreativeModeTab.builder().icon(() -> new ItemStack(ItemsX.RECRIVEN_VI)).title(Component.translatable("itemGroup.ravensmod")).build();
    //?} else {
    /*public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "item_group"));
    public static final CreativeModeTab CUSTOM_ITEM_GROUP = FabricItemGroup.builder().icon(() -> new ItemStack(ItemsX.RECRIVEN_VI)).title(Component.translatable("itemGroup.ravensmod")).build();*/
    //?}
}
