package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.util.GeckoLibUtil;
//?} else {
/*import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;*/
//?}

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CompatBlockItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final Map<Block, String[]> BLOCK_MODELS = new HashMap<>();
    private static final Map<Block, Supplier<?>> RENDERER_FACTORIES = new HashMap<>();

    public static void registerModel(Block block, String modelPath, String texturePath, String animPath) {
        BLOCK_MODELS.put(block, new String[]{modelPath, texturePath, animPath});
    }

    public static String[] getModel(Block block) {
        return BLOCK_MODELS.get(block);
    }

    public static void registerRendererFactory(Block block, Supplier<?> factory) {
        RENDERER_FACTORIES.put(block, factory);
    }

    public static Supplier<?> getRendererFactory(Block block) {
        return RENDERER_FACTORIES.get(block);
    }

    public CompatBlockItem(Block block, Properties properties) {
        super(block, properties);
        //? <26.1 {
        /*SingletonGeoAnimatable.registerSyncedAnimatable(this);*/
        //?}
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }
}
