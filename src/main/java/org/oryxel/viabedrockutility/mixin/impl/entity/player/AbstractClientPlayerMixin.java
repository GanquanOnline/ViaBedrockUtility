package org.oryxel.viabedrockutility.mixin.impl.entity.player;

//? if >=1.21.9 {
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.oryxel.viabedrockutility.payload.PayloadHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerListEntry.class)
public abstract class AbstractClientPlayerMixin {

    @Inject(method = "getSkinTextures", at = @At(value = "TAIL"), cancellable = true)
    public void injectGetSkin(CallbackInfoReturnable<SkinTextures> cir) {
        PlayerListEntry self = (PlayerListEntry) (Object) this;
        if (!ViaBedrockUtility.getInstance().isViaBedrockPresent()) {
            return;
        }

        final PayloadHandler handler = ViaBedrockUtility.getInstance().getPayloadHandler();
        final UUID uuid = self.getProfile().id();

        PayloadHandler.CachedPlayerSkin skinCache = handler.getCachedPlayerSkins().get(uuid);
        Identifier capeId = handler.getCachedPlayerCapes().get(uuid);

        if (skinCache == null && capeId == null) {
            return;
        }

        SkinTextures skin = cir.getReturnValue();
        AssetInfo.TextureAsset body = skin.body();
        AssetInfo.TextureAsset cape = skin.cape();
        PlayerSkinType model = skin.model();
        boolean changed = false;

        if (skinCache != null) {
            PlayerSkinType expected = skinCache.isSlim() ? PlayerSkinType.SLIM : PlayerSkinType.WIDE;
            if (body == null || !skinCache.getTextureId().equals(body.texturePath()) || model != expected) {
                body = new AssetInfo.TextureAssetInfo(skinCache.getTextureId(), skinCache.getTextureId());
                model = expected;
                changed = true;
            }
        }

        if (capeId != null) {
            if (cape == null || !capeId.equals(cape.texturePath())) {
                cape = new AssetInfo.TextureAssetInfo(capeId, capeId);
                changed = true;
            }
        }

        if (changed) {
            cir.setReturnValue(new SkinTextures(body, cape, skin.elytra(), model, skin.secure()));
        }
    }
}
//?} else {
/*import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerMixin extends Entity {
    @Shadow private PlayerListEntry playerListEntry;

    public AbstractClientPlayerMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "getSkinTextures", at = @At(value = "TAIL"), cancellable = true)
    public void injectGetSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (!ViaBedrockUtility.getInstance().isViaBedrockPresent()) {
            return;
        }

        Identifier cape = ViaBedrockUtility.getInstance().getPayloadHandler().getCachedPlayerCapes().get(getUuid());
        if (cape != null) {
            SkinTextures skin = playerListEntry == null ? DefaultSkinHelper.getSkinTextures(this.getUuid()) : playerListEntry.getSkinTextures();
            if (!cape.equals(skin.capeTexture())) {
                cir.setReturnValue(new SkinTextures(
                        skin.texture(),
                        skin.textureUrl(),
                        cape,
                        skin.elytraTexture(),
                        skin.model(),
                        skin.secure()
                ));
            }
        }
    }
}
*///?}
