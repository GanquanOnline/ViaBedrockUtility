package org.oryxel.viabedrockutility.mixin.impl.render;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.oryxel.viabedrockutility.animation.PlayerAnimationManager;
import org.oryxel.viabedrockutility.mixin.interfaces.IBedrockAnimatedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin implements IBedrockAnimatedModel {
    @Unique
    private PlayerAnimationManager animationManager;

    @Override
    public PlayerAnimationManager viaBedrockUtility$getAnimationManager() {
        return this.animationManager;
    }

    @Override
    public void viaBedrockUtility$setAnimationManager(PlayerAnimationManager manager) {
        this.animationManager = manager;
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("TAIL"))
    private void applyBedrockAnimations(PlayerEntityRenderState state, CallbackInfo ci) {
        if (this.animationManager == null || this.animationManager.isEmpty()) {
            return;
        }

        final PlayerEntityModel model = (PlayerEntityModel) (Object) this;

        // Clearing of vanilla rotations and applying Bedrock animations is handled
        // per-animation inside PlayerAnimationManager.animate() to avoid clearing
        // bones that are not targeted by a specific animation.
        animationManager.animate(model, state);
    }
}
