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
    void motDocumentMentioningBothTemplatesUsesArmWidth() {
        final String steveGeo = geometryDocument("geometry.humanoid.custom", 4f);
        assertTrue(steveGeo.contains("customSlim"));
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, steveGeo));
        assertFalse(SkinArmModel.isSlim(SLIM_PATCH, steveGeo));

        final String alexGeo = geometryDocument("geometry.humanoid.customSlim", 3f);
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, alexGeo));
        assertTrue(SkinArmModel.isSlim(WIDE_PATCH, alexGeo));
    }

    @Test
    void identifierOnlyDocumentsDoNotUseSubstringContains() {
        final String steve = "{\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.humanoid.custom\"}}]}";
        final String alex = "{\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.humanoid.customSlim\"}}]}";
        assertFalse(SkinArmModel.isSlim(WIDE_PATCH, steve));
        assertTrue(SkinArmModel.isSlim(SLIM_PATCH, alex));
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
