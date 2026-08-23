package org.oryxel.viabedrockutility.util;

import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;

import java.util.Locale;

/**
 * Stable Java Identifiers for Bedrock pack textures.
 * <p>
 * Vanilla {@code Identifier.of("textures/entity/foo.png")} splits on {@code :} and
 * {@code Identifier.ofVanilla(...)} puts every pack texture into {@code minecraft:}.
 * Either form collides across packs and can be overwritten when the Java resource
 * pack reloads after join, which shows up as NPC skins swapping at random.
 */
public final class BedrockTextureIds {
    private BedrockTextureIds() {
    }

    public static Identifier ofPackTexture(final String bedrockPath) {
        return Identifier.of(ViaBedrockUtilityFabric.MOD_ID, sanitizePath(bedrockPath));
    }

    public static Identifier ofVanillaParticle(final String bedrockPath) {
        return Identifier.ofVanilla(sanitizePath(bedrockPath));
    }

    public static boolean isParticlePath(final String bedrockPath) {
        final String path = sanitizePath(bedrockPath);
        return path.equals("textures/particle") || path.startsWith("textures/particle/");
    }

    public static Identifier ofPlayerSkin(final java.util.UUID uuid) {
        return Identifier.of(ViaBedrockUtilityFabric.MOD_ID, "skin/" + uuid);
    }

    public static Identifier ofCape(final String capeId) {
        return Identifier.of(ViaBedrockUtilityFabric.MOD_ID, "cape/" + sanitizePath(capeId == null || capeId.isBlank() ? "unknown" : capeId));
    }

    public static Identifier ofSkinAnimation(final java.util.UUID uuid, final int animIndex) {
        return Identifier.of(ViaBedrockUtilityFabric.MOD_ID, "skin/" + uuid + "/anim_" + animIndex);
    }

    public static String sanitizePath(final String raw) {
        String path = raw == null ? "" : raw.trim().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith(".png") || path.endsWith(".tga") || path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            path = path.substring(0, path.lastIndexOf('.'));
        }
        path = path.replace(':', '/');
        final StringBuilder builder = new StringBuilder(path.length());
        boolean slash = false;
        for (int i = 0; i < path.length(); i++) {
            final char c = path.charAt(i);
            if (c == '/') {
                if (builder.length() == 0 || slash) {
                    continue;
                }
                builder.append('/');
                slash = true;
                continue;
            }
            slash = false;
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        while (builder.length() > 0 && builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }
        if (builder.length() == 0) {
            return "textures/unknown";
        }
        final String sanitized = builder.toString();
        if (sanitized.equals("textures") || sanitized.startsWith("textures/")) {
            return sanitized;
        }
        return "textures/" + sanitized;
    }
}
