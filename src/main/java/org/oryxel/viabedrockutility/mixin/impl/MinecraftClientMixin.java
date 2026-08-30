package org.oryxel.viabedrockutility.mixin.impl;

import nakern.be_camera.camera.CameraManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void disconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        ViaBedrockUtility.getInstance().setViaBedrockPresent(false);
        ViaBedrockUtility.getInstance().setSpectatorNoclip(false);
        if (ViaBedrockUtility.getInstance().getPayloadHandler() == null) {
            return;
        }

        ViaBedrockUtility.getInstance().getPayloadHandler().clearSessionCaches();

        // Reset BECamera state
        CameraManager.INSTANCE.resetAll();

        // Clear BEParticle emitters (keep definitions for reconnect)
        net.easecation.beparticle.ParticleManager.INSTANCE.clearEmitters();
    }
}
