package org.oryxel.viabedrockutility.pack.processor;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;
import org.oryxel.viabedrockutility.util.BedrockTextureIds;
import net.easecation.bedrockmotion.pack.content.Content;

import java.io.IOException;
import java.util.List;

public class TextureProcessor {
    public static void process(final List<Content> packs) {
        final MinecraftClient client = MinecraftClient.getInstance();
        final Runnable register = () -> registerTextures(client, packs);
        if (client.isOnThread()) {
            register.run();
        } else {
            client.execute(register);
        }
    }

    private static void registerTextures(final MinecraftClient client, final List<Content> packs) {
        int registered = 0;
        for (final Content content : packs) {
            for (final String path : content.getFilesDeep("textures/", "")) {
                final Content.LazyImage image = content.getShortnameImage(path);
                if (image == null) {
                    continue;
                }

                try {
                    final byte[] png = image.getPngBytes();
                    final Identifier identifier = BedrockTextureIds.ofPackTexture(path);
                    final NativeImage image1 = NativeImage.read(png);
                    client.getTextureManager().registerTexture(identifier, new NativeImageBackedTexture(() -> identifier.toString() + image1.hashCode(), image1));
                    registered++;
                    // BEParticle still resolves Bedrock particle paths as minecraft:textures/particle/...
                    if (BedrockTextureIds.isParticlePath(path)) {
                        final Identifier vanilla = BedrockTextureIds.ofVanillaParticle(path);
                        final NativeImage particleCopy = NativeImage.read(png);
                        client.getTextureManager().registerTexture(vanilla, new NativeImageBackedTexture(() -> vanilla.toString() + particleCopy.hashCode(), particleCopy));
                    }
                } catch (final IllegalArgumentException e) {
                    ViaBedrockUtilityFabric.LOGGER.warn("Unable to register texture {} as a Java identifier", path);
                } catch (final IOException e) {
                    ViaBedrockUtilityFabric.LOGGER.warn("Unable to register texture {}", path);
                }
            }
        }
        ViaBedrockUtilityFabric.LOGGER.info("[ResourcePack] Registered {} bedrock pack texture(s) under {}", registered, ViaBedrockUtilityFabric.MOD_ID);
    }
}
