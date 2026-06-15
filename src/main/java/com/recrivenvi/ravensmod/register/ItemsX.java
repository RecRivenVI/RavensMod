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
import net.minecraft.world.item.Item;

import java.util.function.Function;

import static com.recrivenvi.ravensmod.register.TabsX.CUSTOM_ITEM_GROUP;
import static com.recrivenvi.ravensmod.register.TabsX.CUSTOM_ITEM_GROUP_KEY;

public class ItemsX {
    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        //? >=26.1 {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(RavensMod.MOD_ID, name));
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        //?} else {
        /*ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, id, item);*/
        //?}
        return item;
    }
    public static final Item RECRIVEN_VI = register("recriven_vi", Item::new, new Item.Properties());

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
        //? >=26.1 {
        CreativeModeTabEvents.modifyOutputEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.accept(ItemsX.RECRIVEN_VI);
        });
        //?} else {
        /*ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(entries -> {
            entries.accept(ItemsX.RECRIVEN_VI);
        });*/
        //?}
    }
}
