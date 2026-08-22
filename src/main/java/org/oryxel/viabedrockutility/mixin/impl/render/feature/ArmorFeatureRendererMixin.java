package org.oryxel.viabedrockutility.mixin.impl.render.feature;

import net.minecraft.client.model.Model;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;
import org.oryxel.viabedrockutility.util.BipedModelSync;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin {
    //? if >=1.21.9 {
    @ModifyArg(
            method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/equipment/EquipmentRenderer;render(Lnet/minecraft/client/render/entity/equipment/EquipmentModel$LayerType;Lnet/minecraft/registry/RegistryKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;II)V"),
            index = 2
    )
    private Model<?> viaBedrockUtility$syncCustomPlayerPose(final Model<?> armorModel) {
        final FeatureRenderer<?, ?> self = (FeatureRenderer<?, ?>) (Object) this;
        if (!(self.getContextModel() instanceof PlayerEntityModel playerModel)) {
            return armorModel;
        }
        if (!((IModelPart) (Object) playerModel.getRootPart()).viaBedrockUtility$isVBUModel()) {
            return armorModel;
        }
        if (armorModel instanceof BipedEntityModel<?> bipedArmor) {
            BipedModelSync.copyPose(playerModel, bipedArmor);
        }
        return armorModel;
    }
    //?} else {
    /*@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/equipment/EquipmentRenderer;render"), index = 2)
    private Model<?> viaBedrockUtility$syncCustomPlayerPose(final Model<?> armorModel) {
        return armorModel;
    }
    *///?}
}
