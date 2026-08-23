package org.oryxel.viabedrockutility.util;

import com.google.gson.JsonArray;
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
        final String required = defaultGeometry(resourcePatch);
        if (isSlimGeometryName(required)) {
            return true;
        }
        if (isWideGeometryName(required)) {
            return false;
        }
        final Float armWidth = armWidthFromGeometryJson(geometryRaw, required);
        if (armWidth != null) {
            return armWidth < 3.5f;
        }
        return geometryRaw != null && looksLikeSlimIdentifier(geometryRaw) && !looksLikeWideIdentifier(geometryRaw);
    }

    public static boolean isSlim(final String resourcePatch, final String geometryRaw, final BedrockGeometryModel selected) {
        final String required = defaultGeometry(resourcePatch);
        if (isSlimGeometryName(required)) {
            return true;
        }
        if (isWideGeometryName(required)) {
            return false;
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
        return isSlim(resourcePatch, geometryRaw);
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

    private static boolean looksLikeSlimIdentifier(final String geometryRaw) {
        return geometryRaw.contains("\"" + SLIM_GEOMETRY + "\"")
                || geometryRaw.contains("'" + SLIM_GEOMETRY + "'")
                || geometryRaw.contains(SLIM_GEOMETRY + ".");
    }

    private static boolean looksLikeWideIdentifier(final String geometryRaw) {
        return geometryRaw.contains("\"" + WIDE_GEOMETRY + "\"")
                || geometryRaw.contains("'" + WIDE_GEOMETRY + "'")
                || geometryRaw.contains(WIDE_GEOMETRY + ".");
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

    private static Float armWidthFromGeometryJson(final String geometryRaw, final String requiredGeometry) {
        if (geometryRaw == null || geometryRaw.isBlank()) {
            return null;
        }
        try {
            final JsonElement root = JsonParser.parseString(geometryRaw);
            final JsonObject selected = selectGeometryObject(root, requiredGeometry);
            final float minWidth = scanForArmWidth(selected != null ? selected : root);
            return minWidth == Float.MAX_VALUE ? null : minWidth;
        } catch (final Exception ignored) {
            return null;
        }
    }

    private static JsonObject selectGeometryObject(final JsonElement element, final String requiredGeometry) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonArray()) {
            JsonObject humanoid = null;
            for (final JsonElement child : element.getAsJsonArray()) {
                final JsonObject match = selectGeometryObject(child, requiredGeometry);
                if (match == null) {
                    continue;
                }
                final String identifier = geometryIdentifier(match);
                if (requiredGeometry != null && requiredGeometry.equals(identifier)) {
                    return match;
                }
                if (isClassicHumanoid(identifier) && humanoid == null) {
                    humanoid = match;
                }
            }
            return humanoid;
        }
        if (!element.isJsonObject()) {
            return null;
        }
        final JsonObject object = element.getAsJsonObject();
        if (object.has("minecraft:geometry")) {
            return selectGeometryObject(object.get("minecraft:geometry"), requiredGeometry);
        }
        final String identifier = geometryIdentifier(object);
        if (requiredGeometry != null && requiredGeometry.equals(identifier)) {
            return object;
        }
        if (isClassicHumanoid(identifier) && object.has("bones")) {
            return object;
        }
        return null;
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

    private static float scanForArmWidth(final JsonElement element) {
        float minWidth = Float.MAX_VALUE;
        if (element == null || element.isJsonNull()) {
            return minWidth;
        }
        if (element.isJsonArray()) {
            for (final JsonElement child : element.getAsJsonArray()) {
                minWidth = Math.min(minWidth, scanForArmWidth(child));
            }
            return minWidth;
        }
        if (!element.isJsonObject()) {
            return minWidth;
        }
        final JsonObject object = element.getAsJsonObject();
        if (object.has("minecraft:geometry")) {
            minWidth = Math.min(minWidth, scanForArmWidth(object.get("minecraft:geometry")));
        }
        if (object.has("bones")) {
            minWidth = Math.min(minWidth, scanForArmWidth(object.get("bones")));
        }
        final String name = object.has("name") && object.get("name").isJsonPrimitive()
                ? object.get("name").getAsString()
                : "";
        if (isArmBone(name) && object.has("cubes") && object.get("cubes").isJsonArray()) {
            final JsonArray cubes = object.getAsJsonArray("cubes");
            for (final JsonElement cubeElement : cubes) {
                if (!cubeElement.isJsonObject()) {
                    continue;
                }
                final JsonObject cube = cubeElement.getAsJsonObject();
                if (!cube.has("size") || !cube.get("size").isJsonArray() || cube.getAsJsonArray("size").isEmpty()) {
                    continue;
                }
                final float width = Math.abs(cube.getAsJsonArray("size").get(0).getAsFloat());
                if (width > 0.1f && width < minWidth) {
                    minWidth = width;
                }
            }
        }
        return minWidth;
    }

    private static boolean isArmBone(final String name) {
        if (name == null) {
            return false;
        }
        final String lower = name.toLowerCase(Locale.ROOT).replace("_", "");
        return lower.equals("leftarm") || lower.equals("rightarm");
    }
}
