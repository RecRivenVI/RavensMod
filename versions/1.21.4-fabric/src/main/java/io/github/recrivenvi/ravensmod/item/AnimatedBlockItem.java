package io.github.recrivenvi.ravensmod.item;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AnimatedBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final Map<Block, GeoRenderProviderFactory<?>> RENDER_PROVIDERS = new HashMap<>();

    public static void registerRenderProvider(Block block, GeoRenderProviderFactory<?> provider) {
        RENDER_PROVIDERS.put(block, provider);
    }

    public AnimatedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void createGeoRenderer(Consumer consumer) {
        GeoRenderProviderFactory provider = RENDER_PROVIDERS.get(getBlock());
        if (provider != null) {
            provider.accept(consumer);
        }
    }
}
