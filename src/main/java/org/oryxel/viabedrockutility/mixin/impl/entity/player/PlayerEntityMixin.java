package org.oryxel.viabedrockutility.mixin.impl.entity.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    // Vanilla does noClip = isSpectator() at the start of tick (then setOnGround(false) for spectators).
    // Local MOT spectator is Java ADVENTURE, so force those two calls to behave like spectator.
    // Do not override PlayerEntity.isSpectator() itself — that would lock the inventory HUD.
    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z"),
            require = 2,
            expect = 2
    )
    private boolean viaBedrockUtility$treatLocalNoclipAsSpectator(PlayerEntity self) {
        if (ViaBedrockUtility.getInstance().isViaBedrockPresent()
                && ViaBedrockUtility.getInstance().isSpectatorNoclip()) {
            final MinecraftClient client = MinecraftClient.getInstance();
            if (client != null && client.player == self) {
                return true;
            }
        }
        return self.isSpectator();
    }
}
