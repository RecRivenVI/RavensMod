package com.recrivenvi.ravensmod.client;

//? >=26.1 {
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.model.GeoModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
//?} else {
/*import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.AnimatedGeoModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;*/
//?}

import com.recrivenvi.ravensmod.compat.CompatBlockEntitiesModels;
import org.jetbrains.annotations.Nullable;

//? >=26.1 {
public class BlockEntitiesModels<T extends BlockEntity & GeoAnimatable> extends CompatBlockEntitiesModels<T> {
    public BlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
        super(modelPath, texturePath, animPath);
    }
}
//?} else {
/*public class BlockEntitiesModels<T extends BlockEntity & GeoAnimatable> extends CompatBlockEntitiesModels<T> {
    public BlockEntitiesModels(String modelPath, String texturePath, @Nullable String animPath) {
        super(modelPath, texturePath, animPath);
    }
}*/
//?}
