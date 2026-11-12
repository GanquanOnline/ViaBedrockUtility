package org.oryxel.viabedrockutility.mixin.impl.render;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.ArmedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
//? if >=1.21.9 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//?} else {
/*import net.minecraft.client.render.VertexConsumerProvider;
*///?}
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bedrock geometry player models anchor held items through
 * {@link org.oryxel.viabedrockutility.renderer.VbuHeldItemLayer} instead, because the vanilla anchor
 * assumes an arm origin placed on the shoulder.
 */
@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemFeatureRendererMixin {
    //? if >=1.21.9 {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void viaBedrockUtility$skipForBedrockModels(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ArmedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        viaBedrockUtility$cancelForBedrockModel(ci);
    }
    //?} else {
    /*@Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void viaBedrockUtility$skipForBedrockModels(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ArmedEntityRenderState state, float limbAngle, float limbDistance, CallbackInfo ci) {
        viaBedrockUtility$cancelForBedrockModel(ci);
    }
    *///?}

    private void viaBedrockUtility$cancelForBedrockModel(CallbackInfo ci) {
        final FeatureRenderer<?, ?> self = (FeatureRenderer<?, ?>) (Object) this;
        if (!(self.getContextModel() instanceof PlayerEntityModel model)) {
            return;
        }
        if (((IModelPart) (Object) model.getRootPart()).viaBedrockUtility$isVBUModel()) {
            ci.cancel();
        }
    }
}
