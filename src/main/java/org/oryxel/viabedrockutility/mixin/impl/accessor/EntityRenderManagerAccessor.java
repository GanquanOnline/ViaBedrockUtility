package org.oryxel.viabedrockutility.mixin.impl.accessor;

import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// Custom player/entity renderers are constructed at runtime and must reuse the
// already-loaded equipment models. A fresh EquipmentModelLoader has an empty map,
// which makes armor, elytra and similar layers silently skip rendering.
//? if >=1.21.9 {
@Mixin(net.minecraft.client.render.entity.EntityRenderManager.class)
//?} else {
/*@Mixin(net.minecraft.client.render.entity.EntityRenderDispatcher.class)
*///?}
public interface EntityRenderManagerAccessor {
    @Accessor("equipmentModelLoader")
    EquipmentModelLoader viaBedrockUtility$getEquipmentModelLoader();
}
