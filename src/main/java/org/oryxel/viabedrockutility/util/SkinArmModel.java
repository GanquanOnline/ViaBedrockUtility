package org.oryxel.viabedrockutility.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.cube.converter.model.element.Cube;
import org.cube.converter.model.element.Parent;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;

import java.util.List;
import java.util.Locale;

/**
 * Distinguishes Java slim (Alex, 3px) vs wide (Steve, 4px) arms for Bedrock player skins.
 * <p>
 * NetEase ConfirmSkin ships a full {@code minecraft:geometry} document whose first
 * entry is often {@code geometry.cape}. Substring checks such as
 * {@code contains("customSlim")} or {@code startsWith("geometry.humanoid.custom")}
 * therefore mis-classify skins: {@code customSlim} itself starts with {@code custom}.
 */
public final class SkinArmModel {
    public static final String WIDE_GEOMETRY = "geometry.humanoid.custom";
    public static final String SLIM_GEOMETRY = "geometry.humanoid.customSlim";

    private SkinArmModel() {
    }

    public static String defaultGeometry(final String resourcePatch) {
        if (resourcePatch == null || resourcePatch.isBlank()) {
            return null;
        }
        try {
            final JsonObject root = JsonParser.parseString(resourcePatch).getAsJsonObject();
            final JsonObject geometry = root.getAsJsonObject("geometry");
            if (geometry == null || !geometry.has("default")) {
                return null;
            }
            final String value = geometry.get("default").getAsString();
            return value == null || value.isBlank() ? null : value;
        } catch (final Exception ignored) {
            return null;
        }
    }

    public static boolean isSlimGeometryName(final String name) {
        return matchesGeometryName(name, SLIM_GEOMETRY);
    }

    public static boolean isWideGeometryName(final String name) {
        return matchesGeometryName(name, WIDE_GEOMETRY) && !isSlimGeometryName(name);
    }

    public static boolean isClassicHumanoid(final String name) {
        return isSlimGeometryName(name) || isWideGeometryName(name);
    }

    public static boolean isSlim(final String resourcePatch, final String geometryRaw) {
        return isSlim(resourcePatch, geometryRaw, null, null, 0, 0);
    }

    public static boolean isSlim(final String resourcePatch, final String geometryRaw, final BedrockGeometryModel selected) {
        return isSlim(resourcePatch, geometryRaw, selected, null, 0, 0);
    }

    public static boolean isSlim(final String resourcePatch, final String geometryRaw, final byte[] skinRgba, final int width, final int height) {
        return isSlim(resourcePatch, geometryRaw, null, skinRgba, width, height);
    }

    /**
     * MOT default geometry includes both Steve and Alex templates. Prefer an explicit
     * {@code skinResourcePatch}, then an unambiguous identifier, then the unused 4th
     * Steve arm column in the 64x64 texture. Never take the smallest arm cube across
     * the whole document.
     */
    public static boolean isSlim(final String resourcePatch, final String geometryRaw, final BedrockGeometryModel selected,
                                 final byte[] skinRgba, final int width, final int height) {
        final String required = defaultGeometry(resourcePatch);
        if (isSlimGeometryName(required)) {
            return true;
        }
        if (isWideGeometryName(required)) {
            return false;
        }
        final Boolean fromGeometry = slimFromUnambiguousGeometry(geometryRaw);
        if (fromGeometry != null) {
            return fromGeometry;
        }
        final Boolean fromTexture = slimFromRgba(skinRgba, width, height);
        if (fromTexture != null) {
            return fromTexture;
        }
        if (selected != null) {
            final Float armWidth = armWidthFromModel(selected);
            if (armWidth != null) {
                return armWidth < 3.5f;
            }
            if (isSlimGeometryName(selected.getIdentifier())) {
                return true;
            }
            if (isWideGeometryName(selected.getIdentifier())) {
                return false;
            }
        }
        return false;
    }

    public static String requiredGeometry(final String resourcePatch, final boolean slim) {
        final String required = defaultGeometry(resourcePatch);
        if (required != null && !required.isBlank()) {
            return required;
        }
        return slim ? SLIM_GEOMETRY : WIDE_GEOMETRY;
    }

    public static Boolean slimFromRgba(final byte[] data, final int width, final int height) {
        if (data == null || width <= 0 || height <= 0 || data.length < width * height * 4) {
            return null;
        }
        if (width < 64 || height < 32) {
            return null;
        }
        if (height < 64) {
            return Boolean.FALSE;
        }
        final int scale = Math.max(1, width / 64);
        int opaque = 0;
        int samples = 0;
        for (int dy = 0; dy < 12; dy++) {
            for (int sy = 0; sy < scale; sy++) {
                for (int sx = 0; sx < scale; sx++) {
                    final int x = 43 * scale + sx;
                    final int y = 20 * scale + dy * scale + sy;
                    if (x >= width || y >= height) {
                        continue;
                    }
                    samples++;
                    final int index = (y * width + x) * 4;
                    if ((data[index + 3] & 0xFF) > 128) {
                        opaque++;
                    }
                }
            }
        }
        if (samples == 0) {
            return null;
        }
        final float ratio = opaque / (float) samples;
        if (ratio < 0.15f) {
            return Boolean.TRUE;
        }
        if (ratio > 0.50f) {
            return Boolean.FALSE;
        }
        return null;
    }

    public static BedrockGeometryModel selectPlayerGeometry(final List<BedrockGeometryModel> geometries, final String requiredGeometry) {
        if (geometries == null || geometries.isEmpty()) {
            return null;
        }
        if (requiredGeometry != null) {
            for (final BedrockGeometryModel geometry : geometries) {
                if (requiredGeometry.equals(geometry.getIdentifier())) {
                    return geometry;
                }
            }
        }
        BedrockGeometryModel humanoid = null;
        for (final BedrockGeometryModel geometry : geometries) {
            final String identifier = geometry.getIdentifier();
            if (identifier == null) {
                continue;
            }
            if (isClassicHumanoid(identifier)) {
                if (requiredGeometry != null && isSlimGeometryName(requiredGeometry) && isSlimGeometryName(identifier)) {
                    return geometry;
                }
                if (requiredGeometry != null && isWideGeometryName(requiredGeometry) && isWideGeometryName(identifier)) {
                    return geometry;
                }
                if (humanoid == null) {
                    humanoid = geometry;
                }
            }
        }
        if (humanoid != null) {
            return humanoid;
        }
        for (final BedrockGeometryModel geometry : geometries) {
            final String identifier = geometry.getIdentifier();
            if (identifier != null && identifier.toLowerCase(Locale.ROOT).contains("cape")) {
                continue;
            }
            return geometry;
        }
        return null;
    }

    private static boolean matchesGeometryName(final String name, final String expected) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return name.equals(expected) || name.startsWith(expected + ".");
    }

    private static Boolean slimFromUnambiguousGeometry(final String geometryRaw) {
        if (geometryRaw == null || geometryRaw.isBlank()) {
            return null;
        }
        final String trimmed = geometryRaw.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            if (isSlimGeometryName(trimmed)) {
                return Boolean.TRUE;
            }
            if (isWideGeometryName(trimmed)) {
                return Boolean.FALSE;
            }
            return null;
        }
        try {
            final JsonElement root = JsonParser.parseString(trimmed);
            final boolean[] found = new boolean[2];
            collectHumanoidIdentifiers(root, found);
            if (found[0] && !found[1]) {
                return Boolean.TRUE;
            }
            if (found[1] && !found[0]) {
                return Boolean.FALSE;
            }
            return null;
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static void collectHumanoidIdentifiers(final JsonElement element, final boolean[] found) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonArray()) {
            for (final JsonElement child : element.getAsJsonArray()) {
                collectHumanoidIdentifiers(child, found);
            }
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        final JsonObject object = element.getAsJsonObject();
        if (object.has("minecraft:geometry")) {
            collectHumanoidIdentifiers(object.get("minecraft:geometry"), found);
            return;
        }
        final String identifier = geometryIdentifier(object);
        if (isSlimGeometryName(identifier)) {
            found[0] = true;
        } else if (isWideGeometryName(identifier)) {
            found[1] = true;
        }
    }

    private static Float armWidthFromModel(final BedrockGeometryModel geometry) {
        float minWidth = Float.MAX_VALUE;
        for (final Parent bone : geometry.getParents()) {
            if (!isArmBone(bone.getName())) {
                continue;
            }
            for (final Cube cube : bone.getCubes().values()) {
                final float width = Math.abs(cube.getSize().getX());
                if (width > 0.1f && width < minWidth) {
                    minWidth = width;
                }
            }
        }
        return minWidth == Float.MAX_VALUE ? null : minWidth;
    }

    private static String geometryIdentifier(final JsonObject object) {
        if (object.has("description") && object.get("description").isJsonObject()) {
            final JsonObject description = object.getAsJsonObject("description");
            if (description.has("identifier") && description.get("identifier").isJsonPrimitive()) {
                return description.get("identifier").getAsString();
            }
        }
        if (object.has("identifier") && object.get("identifier").isJsonPrimitive()) {
            return object.get("identifier").getAsString();
        }
        return null;
    }

    private static boolean isArmBone(final String name) {
        if (name == null) {
            return false;
        }
        final String lower = name.toLowerCase(Locale.ROOT).replace("_", "");
        return lower.equals("leftarm") || lower.equals("rightarm");
    }
}
