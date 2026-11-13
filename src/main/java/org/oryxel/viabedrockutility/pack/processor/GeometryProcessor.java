package org.oryxel.viabedrockutility.pack.processor;

import net.easecation.bedrockmotion.pack.PackManager;
import net.easecation.bedrockmotion.pack.content.Content;
import org.cube.converter.model.impl.bedrock.BedrockGeometryModel;
import org.cube.converter.parser.bedrock.geometry.BedrockGeometryParser;
import org.oryxel.viabedrockutility.fabric.ViaBedrockUtilityFabric;

import java.util.List;
import java.util.Map;

/**
 * 网易 NPC 的骨架写在皮肤文件里（{@code <皮肤名>/skin.json}），不在 {@code models/entity/}
 * 下。BedrockMotion 只扫后者，这类骨架就查不到，MODEL_REQUEST 拿到 geometry 后找不到模型。
 * 这里把包里其余带 {@code minecraft:geometry} 的 JSON 补进模型表；已有同名定义时保留原定义。
 */
public final class GeometryProcessor {

    private static final String GEOMETRY_MARKER = "minecraft:geometry";

    private GeometryProcessor() {
    }

    public static void process(final PackManager packManager, final List<Content> packs) {
        if (packManager == null || packManager.getModelDefinitions() == null) {
            return;
        }
        final Map<String, BedrockGeometryModel> models = packManager.getModelDefinitions().getEntityModels();
        int added = 0;
        for (final Content content : packs) {
            for (final String path : content.getFilesDeep("", ".json")) {
                if (path.startsWith("models/")) {
                    continue; // BedrockMotion 已经解析过
                }
                try {
                    final String json = content.getString(path);
                    if (json == null || !json.contains(GEOMETRY_MARKER)) {
                        continue;
                    }
                    for (final BedrockGeometryModel geometry : BedrockGeometryParser.parse(json)) {
                        if (geometry.getIdentifier() == null || geometry.getIdentifier().isEmpty()) {
                            continue;
                        }
                        if (models.putIfAbsent(geometry.getIdentifier(), geometry) == null) {
                            added++;
                            ViaBedrockUtilityFabric.LOGGER.debug("[Geometry] Registered '{}' from {}", geometry.getIdentifier(), path);
                        }
                    }
                } catch (final Throwable e) {
                    ViaBedrockUtilityFabric.LOGGER.debug("[Geometry] Skipped {}: {}", path, e.getMessage());
                }
            }
        }
        if (added > 0) {
            ViaBedrockUtilityFabric.LOGGER.info("[Geometry] Registered {} skin-file geometry definition(s)", added);
        }
    }
}
