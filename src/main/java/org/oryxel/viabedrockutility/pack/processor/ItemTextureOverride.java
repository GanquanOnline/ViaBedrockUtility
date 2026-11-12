package org.oryxel.viabedrockutility.pack.processor;

import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Copies bedrock item PNGs onto the Java item textures the vanilla model already uses.
 * A music disc stays {@code minecraft:music_disc_blocks}; only the PNG changes.
 */
public final class ItemTextureOverride {

    private ItemTextureOverride() {
    }

    public static void apply(Path packZip) {
        Map<String, byte[]> additions = collect(packZip);
        if (additions.isEmpty()) {
            return;
        }
        try (FileSystem fs = FileSystems.newFileSystem(packZip)) {
            int written = 0;
            for (Map.Entry<String, byte[]> entry : additions.entrySet()) {
                Path target = fs.getPath(entry.getKey());
                if (Files.exists(target)) {
                    continue;
                }
                Path parent = target.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.write(target, entry.getValue());
                written++;
            }
            if (written > 0) {
                ViaBedrockUtilityFabric.LOGGER.info("[ResourcePack] Wrote {} bedrock item texture override(s)", written);
            }
        } catch (IOException e) {
            ViaBedrockUtilityFabric.LOGGER.warn("[ResourcePack] Failed to write item texture overrides into {}", packZip.getFileName(), e);
        }
    }

    private static Map<String, byte[]> collect(Path packZip) {
        Map<String, byte[]> additions = new LinkedHashMap<>();
        if (!Files.isRegularFile(packZip)) {
            return additions;
        }
        try (ZipFile zip = new ZipFile(packZip.toFile())) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory() || !name.startsWith("bedrock/") || !name.endsWith(".mcpack")) {
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    collectMcpack(in.readAllBytes(), additions);
                }
            }
        } catch (IOException e) {
            ViaBedrockUtilityFabric.LOGGER.warn("[ResourcePack] Failed to read {} for item textures", packZip.getFileName(), e);
        }
        return additions;
    }

    private static void collectMcpack(byte[] mcpack, Map<String, byte[]> additions) throws IOException {
        Path temp = Files.createTempFile("tbmg-mcpack", ".zip");
        try {
            Files.write(temp, mcpack);
            try (ZipFile zip = new ZipFile(temp.toFile())) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName().replace('\\', '/');
                    if (entry.isDirectory() || !name.startsWith("textures/items/") || !name.endsWith(".png")) {
                        continue;
                    }
                    String file = name.substring("textures/items/".length());
                    String javaName = javaItemTexture(file.substring(0, file.length() - 4));
                    if (javaName == null) {
                        continue;
                    }
                    try (InputStream in = zip.getInputStream(entry)) {
                        additions.putIfAbsent("assets/minecraft/textures/item/" + javaName + ".png", in.readAllBytes());
                    }
                }
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Bedrock {@code record_*} files are the Java {@code music_disc_*} icons. */
    static String javaItemTexture(String bedrockFileName) {
        String name = bedrockFileName.toLowerCase(Locale.ROOT);
        if (name.startsWith("record_")) {
            return "music_disc_" + name.substring("record_".length());
        }
        return null;
    }
}
