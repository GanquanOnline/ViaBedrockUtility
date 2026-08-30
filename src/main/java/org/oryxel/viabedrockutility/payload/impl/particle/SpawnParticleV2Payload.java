package org.oryxel.viabedrockutility.payload.impl.particle;

import lombok.Getter;
import org.oryxel.viabedrockutility.payload.BasePayload;

import java.util.UUID;

@Getter
public class SpawnParticleV2Payload extends BasePayload {
    private final String identifier;
    private final int anchorKind;
    private final UUID ownerUuid;
    private final float x, y, z;
    private final String molangVarsJson;

    public SpawnParticleV2Payload(String identifier, int anchorKind, UUID ownerUuid, float x, float y, float z, String molangVarsJson) {
        this.identifier = identifier;
        this.anchorKind = anchorKind;
        this.ownerUuid = ownerUuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.molangVarsJson = molangVarsJson;
    }
}
