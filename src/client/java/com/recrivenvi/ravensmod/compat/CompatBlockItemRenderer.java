package com.recrivenvi.ravensmod.compat;

//? >=26.1 {
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoItemRenderer;
//?} else {
/*import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;*/
//?}

public class CompatBlockItemRenderer extends GeoItemRenderer<CompatBlockItem> {
    public CompatBlockItemRenderer(GeoModel<CompatBlockItem> model) {
        super(model);
    }
}
