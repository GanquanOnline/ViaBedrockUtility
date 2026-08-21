package org.oryxel.viabedrockutility.payload.impl.entity;

import lombok.Getter;
import org.oryxel.viabedrockutility.payload.BasePayload;

import java.util.UUID;

@Getter
public class AnimateEntityPayload extends BasePayload {
    private final UUID uuid;
    private final String animationName;

    public AnimateEntityPayload(UUID uuid, String animationName) {
        this.uuid = uuid;
        this.animationName = animationName;
    }
}
