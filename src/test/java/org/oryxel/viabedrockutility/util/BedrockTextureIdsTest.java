package org.oryxel.viabedrockutility.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BedrockTextureIdsTest {
    @Test
    void sanitizesColonAndExtensionWithoutLeavingMinecraftNamespace() {
        assertEquals("textures/entity/npc/npc_1", BedrockTextureIds.sanitizePath("textures/entity/npc/npc_1.png"));
        assertEquals("textures/entity/npc/npc_1", BedrockTextureIds.sanitizePath("textures/entity/npc:npc_1.png"));
        assertEquals("textures/entity/foo_bar", BedrockTextureIds.sanitizePath("textures/entity/foo bar.png"));
        assertEquals("textures/entity/foo", BedrockTextureIds.sanitizePath("/textures/entity/foo.PNG"));
        assertEquals("textures/unknown", BedrockTextureIds.sanitizePath(":::"));
        assertTrue(BedrockTextureIds.isParticlePath("textures/particle/particles.png"));
        assertTrue(BedrockTextureIds.isParticlePath("textures/particle/campfire_smoke"));
        assertFalse(BedrockTextureIds.isParticlePath("textures/entity/npc/npc_1.png"));
    }
}
