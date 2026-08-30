package org.oryxel.viabedrockutility.payload;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.ViaBedrockUtility;
import org.oryxel.viabedrockutility.enums.bedrock.ActorFlags;
import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;
import org.oryxel.viabedrockutility.payload.enums.PayloadType;
import org.oryxel.viabedrockutility.payload.impl.entity.*;
import org.oryxel.viabedrockutility.payload.impl.particle.*;
import org.oryxel.viabedrockutility.payload.impl.skin.*;
import org.oryxel.viabedrockutility.util.EnumUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Getter
public class BasePayload implements CustomPayload {
    public static Id<BasePayload> ID = new Id<>(Identifier.of(ViaBedrockUtilityFabric.MOD_ID, "data"));

    public static final PacketCodec<PacketByteBuf, BasePayload> STREAM_CODEC = PacketCodec.of(null, buf -> {
        if (buf.readableBytes() < 4) {
            throw new RuntimeException("VBU payload truncated before type");
        }
        final int type = buf.readInt();
        if (type < 0 || type >= PayloadType.values().length) {
            throw new RuntimeException("Invalid type: " + type);
        }

        final BasePayload decoded = switch (PayloadType.values()[type]) {
            case CONFIRM -> {
                // Confirm that ViaBedrock is present, this should be sent back right after we send confirm register channel.
                ViaBedrockUtility.getInstance().setViaBedrockPresent(true);
                ViaBedrockUtilityFabric.LOGGER.info("[Handshake] Received CONFIRM from ViaBedrock, handshake successful!");
                yield new BasePayload();
            }
            case MODEL_REQUEST -> {
                final java.util.UUID uuid = buf.readUuid();
                final String identifier = readString(buf);

                BigInteger combinedFlags = BigInteger.ZERO;
                if (buf.readBoolean()) {
                    combinedFlags = combinedFlags.add(BigInteger.valueOf(buf.readLong()));
                }
                if (buf.readBoolean()) {
                    combinedFlags = combinedFlags.add(BigInteger.valueOf(buf.readLong()).shiftLeft(64));
                }

                Integer variant = null, mark_variant = null, skinId = null;
                Float scale = null;
                if (buf.readBoolean()) {
                    variant = buf.readInt();
                }
                if (buf.readBoolean()) {
                    mark_variant = buf.readInt();
                }
                if (buf.readBoolean()) {
                    skinId = buf.readInt();
                }
                if (buf.readBoolean()) {
                    scale = buf.readFloat();
                }

                yield new ModelRequestPayload(identifier, EnumUtil.getEnumSetFromBitmask(ActorFlags.class, combinedFlags, ActorFlags::getValue), variant, mark_variant, skinId, scale, uuid);
            }

            case ANIMATE -> {
                // ViaBedrock writes UUID + animation name. Leaving those bytes unread
                // makes vanilla treat the packet as leftover data and disconnect.
                final java.util.UUID uuid = buf.readUuid();
                final String animationName = readString(buf);
                yield new AnimateEntityPayload(uuid, animationName);
            }

            case CAPE -> {
                yield CapeDataPayload.STREAM_DECODER.decode(buf);
            }
            case SKIN_INFORMATION -> {
                yield BaseSkinPayload.STREAM_DECODER.decode(buf);
            }
            case SKIN_DATA -> {
                yield SkinDataPayload.STREAM_DECODER.decode(buf);
            }
            case SKIN_ANIMATION_INFO -> {
                yield SkinAnimationInfoPayload.STREAM_DECODER.decode(buf);
            }
            case SKIN_ANIMATION_DATA -> {
                yield SkinAnimationDataPayload.STREAM_DECODER.decode(buf);
            }

            case SPAWN_PARTICLE -> {
                final String identifier = readString(buf);
                final float x = buf.readFloat();
                final float y = buf.readFloat();
                final float z = buf.readFloat();
                final String molangVarsJson = buf.readBoolean() ? readString(buf) : "";
                ViaBedrockUtilityFabric.LOGGER.info("[Particle:L3] Decoded SPAWN_PARTICLE payload: {} at ({}, {}, {})", identifier, x, y, z);
                yield new SpawnParticlePayload(identifier, x, y, z, molangVarsJson);
            }

            case SPAWN_PARTICLE_V2 -> {
                final String identifier = readString(buf);
                final int anchorKind = buf.readUnsignedByte();
                final java.util.UUID ownerUuid = buf.readBoolean() ? buf.readUuid() : null;
                final float x = buf.readFloat();
                final float y = buf.readFloat();
                final float z = buf.readFloat();
                final String molangVarsJson = buf.readBoolean() ? readString(buf) : "";
                ViaBedrockUtilityFabric.LOGGER.info("[Particle:L3] Decoded SPAWN_PARTICLE_V2 payload: {} anchor={} owner={} at ({}, {}, {})", identifier, anchorKind, ownerUuid, x, y, z);
                yield new SpawnParticleV2Payload(identifier, anchorKind, ownerUuid, x, y, z, molangVarsJson);
            }

            case SPECTATOR_NOCLIP -> {
                yield new org.oryxel.viabedrockutility.payload.impl.SpectatorNoclipPayload(buf.readBoolean());
            }

            default -> throw new IllegalStateException("Unexpected value: " + PayloadType.values()[type]);
        };
        if (buf.readableBytes() > 0) {
            ViaBedrockUtilityFabric.LOGGER.warn("[Payload] Discarding {} leftover byte(s) after decoding type {}", buf.readableBytes(), type);
            buf.skipBytes(buf.readableBytes());
        }
        return decoded;
    });

    public void handle(final PayloadHandler handler) {
        try {
            handler.handle(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public static String readString(PacketByteBuf buf) {
        if (buf.readableBytes() < 4) {
            throw new RuntimeException("VBU string truncated before length");
        }
        int length = buf.readInt();
        if (length < 0 || length > buf.readableBytes()) {
            throw new RuntimeException("VBU string length out of bounds: " + length + " remaining=" + buf.readableBytes());
        }
        String result = buf.toString(buf.readerIndex(), length, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + length);
        return result;
    }
}
