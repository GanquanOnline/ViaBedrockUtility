package org.oryxel.viabedrockutility.renderer;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.mixin.interfaces.ICustomPlayerRendererHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomPlayerRenderer extends PlayerEntityRenderer {
    private final Identifier texture;
    private final List<AnimatedSkinOverlay> overlays = new ArrayList<>();

    public CustomPlayerRenderer(final EntityRendererFactory.Context ctx, final PlayerEntityModel model, final boolean slim, Identifier texture) {
        super(ctx, slim);

        if (model != null) {
            this.model = model;
        }

        this.texture = texture;
        this.addFeature(new AnimatedOverlayFeatureRenderer(this));
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        PlayerEntityRenderState state = super.createRenderState();
        ((ICustomPlayerRendererHolder) state).viaBedrockUtility$setCustomPlayerRenderer(this);
        return state;
    }

    @Override
    public void updateRenderState(final Entity entity, final EntityRenderState state, final float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        if (state instanceof ICustomPlayerRendererHolder holder) {
            holder.viaBedrockUtility$setCustomPlayerRenderer(this);
        }
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState playerEntityRenderState) {
        return this.texture;
    }

    public List<AnimatedSkinOverlay> getOverlays() {
        return Collections.unmodifiableList(overlays);
    }

    public void addOverlay(AnimatedSkinOverlay overlay) {
        this.overlays.add(overlay);
    }

    public void tickOverlays() {
        for (AnimatedSkinOverlay overlay : overlays) {
            overlay.tick();
        }
    }
}
