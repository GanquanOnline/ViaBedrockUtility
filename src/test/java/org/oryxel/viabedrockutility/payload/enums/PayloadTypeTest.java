package org.oryxel.viabedrockutility.payload.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayloadTypeTest {

    @Test
    void spectatorNoclipIsAppendedAfterParticleV2() {
        assertEquals(8, PayloadType.SPAWN_PARTICLE.ordinal());
        assertEquals(9, PayloadType.SPAWN_PARTICLE_V2.ordinal());
        assertEquals(10, PayloadType.SPECTATOR_NOCLIP.ordinal());
    }
}
