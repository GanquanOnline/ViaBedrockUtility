package org.oryxel.viabedrockutility.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkinArmModelTest {
    private static final String WIDE_PATCH = "{\"geometry\":{\"default\":\"geometry.humanoid.custom\"}}";
    private static final String SLIM_PATCH = "{\"geometry\":{\"default\":\"geometry.humanoid.customSlim\"}}";
    private static final String VERSIONED_WIDE_PATCH = "{\"geometry\":{\"default\":\"geometry.humanoid.custom.1742391406.1704\"}}";

    @Test
    void customSlimPatchIsSlimAndDoesNotMatchWidePrefix() {
        assertTrue(SkinArmModel.isSlimGeometryName("geometry.humanoid.customSlim"));
        assertFalse(SkinArmModel.isWideGeometryName("geometry.humanoid.customSlim"));
        assertTrue(SkinArmModel.isWideGeometryName("geometry.humanoid.custom.1742391406.1704"));
        assertFalse(SkinArmModel.isSlimGeometryName("geometry.humanoid.custom.1742391406.1704"));
        assertEquals("geometry.humanoid.customSlim", SkinArmModel.defaultGeometry(SLIM_PATCH));
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, ""));
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, ""));
        assertFalse(SkinArmModel.isSlim(VERSIONED_WIDE_PATCH, ""));
    }

    @Test
    void motDocumentMentioningBothTemplatesUsesPatchOrTexture() {
        final String motDefault = motDocumentWithBothArmTemplates();
        assertTrue(motDefault.contains("customSlim"));
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, motDefault));
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, motDefault));
        assertFalse(SkinArmModel.isSlim("", motDefault));

        final byte[] stevePixels = armColumnRgba(true);
        final byte[] alexPixels = armColumnRgba(false);
        assertFalse(SkinArmModel.isSlim("", motDefault, stevePixels, 64, 64));
        assertTrue(SkinArmModel.isSlim("", motDefault, alexPixels, 64, 64));
        assertEquals("geometry.humanoid.customSlim", SkinArmModel.requiredGeometry("", true));
        assertEquals("geometry.humanoid.custom", SkinArmModel.requiredGeometry("", false));

        final String steveGeo = geometryDocument("geometry.humanoid.custom", 4f);
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, steveGeo));
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, steveGeo));

        final String alexGeo = geometryDocument("geometry.humanoid.customSlim", 3f);
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, alexGeo));
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, alexGeo));
    }

    @Test
    void identifierOnlyDocumentsDoNotUseSubstringContains() {
        final String steve = "{\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.humanoid.custom\"}}]}";
        final String alex = "{\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.humanoid.customSlim\"}}]}";
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, steve));
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, alex));
    }

    private static String motDocumentWithBothArmTemplates() {
        return """
                {
                  "format_version": "1.12.0",
                  "minecraft:geometry": [
                    {"description": {"identifier": "geometry.cape"}},
                    {
                      "description": {"identifier": "geometry.humanoid.custom"},
                      "bones": [
                        {"name": "leftArm", "cubes": [{"size": [4, 12, 4]}]},
                        {"name": "rightArm", "cubes": [{"size": [4, 12, 4]}]}
                      ]
                    },
                    {
                      "description": {"identifier": "geometry.humanoid.customSlim"},
                      "bones": [
                        {"name": "leftArm", "cubes": [{"size": [3, 12, 4]}]},
                        {"name": "rightArm", "cubes": [{"size": [3, 12, 4]}]}
                      ]
                    }
                  ]
                }
                """;
    }

    private static byte[] armColumnRgba(final boolean painted) {
        final byte[] data = new byte[64 * 64 * 4];
        if (!painted) {
            return data;
        }
        for (int y = 20; y < 32; y++) {
            final int index = (y * 64 + 43) * 4;
            data[index] = (byte) 0x33;
            data[index + 1] = (byte) 0x66;
            data[index + 2] = (byte) 0xAA;
            data[index + 3] = (byte) 0xFF;
        }
        return data;
    }

    private static String geometryDocument(final String identifier, final float armWidth) {
        return """
                {
                  "format_version": "1.12.0",
                  "minecraft:geometry": [
                    {"description": {"identifier": "geometry.cape"}},
                    {
                      "description": {"identifier": "%s"},
                      "bones": [
                        {"name": "leftArm", "cubes": [{"origin": [-5, 2, -2], "size": [%s, 12, 4], "uv": [32, 48]}]},
                        {"name": "rightArm", "cubes": [{"origin": [1, 2, -2], "size": [%s, 12, 4], "uv": [40, 16]}]}
                      ]
                    },
                    {"description": {"identifier": "geometry.humanoid.customSlim"}}
                  ]
                }
                """.formatted(identifier, armWidth, armWidth);
    }
}
