package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.util.GeckoLibUtil;
//?} else {
/*import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;*/
//?}

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CompatBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final Map<Block, CompatGeoRenderProviderHolder<?>> RENDER_PROVIDERS = new HashMap<>();

    public static void registerRenderProvider(Block block, CompatGeoRenderProviderHolder<?> provider) {
        RENDER_PROVIDERS.put(block, provider);
    }

    public CompatBlockItem(Block block, Properties properties) {
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
        CompatGeoRenderProviderHolder provider = RENDER_PROVIDERS.get(getBlock());
        if (provider != null) {
            provider.accept(consumer);
        }
    }
}
