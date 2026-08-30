package org.oryxel.viabedrockutility.payload.impl;

import lombok.Getter;
import org.oryxel.viabedrockutility.payload.BasePayload;

@Getter
public class SpectatorNoclipPayload extends BasePayload {
    private final boolean enabled;

    public SpectatorNoclipPayload(boolean enabled) {
        this.enabled = enabled;
    }
}
