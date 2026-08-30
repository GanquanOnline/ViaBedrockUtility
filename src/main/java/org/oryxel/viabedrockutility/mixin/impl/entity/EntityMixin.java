package org.oryxel.viabedrockutility.mixin.impl.entity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow
    private Vec3d pos;

    @Shadow public float distanceTraveled;

    @Inject(method = "setPos", at = @At("HEAD"))
    private void injectSetPos(double x, double y, double z, CallbackInfo ci) {
        if (!(((Object)this) instanceof DisplayEntity.ItemDisplayEntity) || !ViaBedrockUtility.getInstance().isViaBedrockPresent()) {
            return;
        }

        float distanceMoved = (float) new Vec3d(x, y, z).distanceTo(new Vec3d(this.pos.x, y, this.pos.z));
        this.distanceTraveled += distanceMoved;
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void viaBedrockUtility$showSpectatorsToLocalNoclipViewer(PlayerEntity viewer, CallbackInfoReturnable<Boolean> cir) {
        if (!ViaBedrockUtility.getInstance().isViaBedrockPresent() || !ViaBedrockUtility.getInstance().isSpectatorNoclip()) {
            return;
        }
        if (!(((Object) this) instanceof PlayerEntity self) || !self.isSpectator()) {
            return;
        }
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && viewer == client.player) {
            cir.setReturnValue(false);
        }
    }
}
