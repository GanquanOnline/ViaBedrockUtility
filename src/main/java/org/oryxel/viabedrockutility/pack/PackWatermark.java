package org.oryxel.viabedrockutility.pack;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;

/**
 * Draws the bedrock debug-screen watermark (textures/ui/title) at the bottom right.
 * Bedrock JSON UI is not executed on Java, so the image has to be drawn here.
 * The source art is 2006x704; a 128 GUI-pixel square covers the hotbar, so the
 * opaque wordmark is fitted into a small corner box instead.
 */
public final class PackWatermark {

    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/ui/title");
    private static final Identifier LAYER = Identifier.of(ViaBedrockUtilityFabric.MOD_ID, "watermark");
    private static final int TEX_W = 2006;
    private static final int TEX_H = 704;
    private static final float SRC_U = 360;
    private static final float SRC_V = 210;
    private static final int SRC_W = 1280;
    private static final int SRC_H = 450;
    private static final int DRAW_W = 78;
    private static final int DRAW_H = 27;
    private static final int OFFSET_X = 10;
    private static final int OFFSET_Y = 30;
    private static final int COLOR = 0xCCFFFFFF;

    private static volatile boolean available;

    private PackWatermark() {
    }

    public static void markAvailable() {
        available = true;
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, LAYER, (context, tickCounter) -> {
            if (!available) {
                return;
            }
            int x = context.getScaledWindowWidth() - OFFSET_X - DRAW_W;
            int y = context.getScaledWindowHeight() - OFFSET_Y - DRAW_H;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, SRC_U, SRC_V,
                    DRAW_W, DRAW_H, SRC_W, SRC_H, TEX_W, TEX_H, COLOR);
        });
    }
}
