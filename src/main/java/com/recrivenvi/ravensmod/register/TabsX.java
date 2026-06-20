package com.recrivenvi.ravensmod.register;

import com.recrivenvi.ravensmod.RavensMod;
import com.recrivenvi.ravensmod.platform.PlatformRegistries;
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
//? neoforge {
/*import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;*/
//?}

public class TabsX {
    //? neoforge {
    /*public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, RavensMod.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CUSTOM_ITEM_GROUP = CREATIVE_TABS.register("item_group", () -> PlatformRegistries.createCreativeTab(
            () -> new ItemStack(ItemsX.RECRIVEN_VI.get()),
            Component.translatable("itemGroup.ravensmod")));
    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = CUSTOM_ITEM_GROUP.getKey();*/
    //?} else {
    //? >=26.1 {
    public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, "item_group"));
    //?} else {
    /*public static final ResourceKey<CreativeModeTab> CUSTOM_ITEM_GROUP_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, "item_group"));*/
    //?}

    public static final CreativeModeTab CUSTOM_ITEM_GROUP = PlatformRegistries.createCreativeTab(
            () -> new ItemStack(ItemsX.RECRIVEN_VI),
            Component.translatable("itemGroup.ravensmod"));
    //?}
}
