package org.oryxel.viabedrockutility.payload.impl.skin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.payload.BasePayload;
import org.oryxel.viabedrockutility.util.BedrockTextureIds;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public final class CapeDataPayload extends BasePayload {
    public static final PacketDecoder<PacketByteBuf, CapeDataPayload> STREAM_DECODER = buf -> {
        UUID playerUuid = buf.readUuid();
        int width = buf.readInt();
        int height = buf.readInt();

        String capeId = BasePayload.readString(buf);
        Identifier identifier = BedrockTextureIds.ofCape(capeId);

        byte[] capeData = new byte[buf.readInt()];
        buf.readBytes(capeData);
        return new CapeDataPayload(playerUuid, width, height, identifier, capeData);
    };

    private final UUID playerUuid;
    private final int width;
    private final int height;
    private final Identifier identifier;
    private final byte[] capeData;
}