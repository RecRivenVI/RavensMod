package io.github.recrivenvi.ravensmod.registry;

import io.github.recrivenvi.ravensmod.RavensMod;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

import static io.github.recrivenvi.ravensmod.registry.ModCreativeTabs.CUSTOM_ITEM_GROUP;
import static io.github.recrivenvi.ravensmod.registry.ModCreativeTabs.CUSTOM_ITEM_GROUP_KEY;

public class ModItems {
    public static Item register(String name, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(RavensMod.MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        return item;
    }

    public static final Item RECRIVEN_VI = register("recriven_vi", Item::new, new Item.Properties());

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);
    }
}
