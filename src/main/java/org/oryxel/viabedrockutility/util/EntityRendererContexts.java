package org.oryxel.viabedrockutility.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.equipment.EquipmentModelLoader;
import org.oryxel.viabedrockutility.mixin.impl.accessor.EntityRenderManagerAccessor;

public final class EntityRendererContexts {
    private EntityRendererContexts() {
    }

    public static EntityRendererFactory.Context create(final MinecraftClient client) {
        EquipmentModelLoader equipmentLoader = new EquipmentModelLoader();
        if (client.getEntityRenderDispatcher() instanceof EntityRenderManagerAccessor accessor) {
            final EquipmentModelLoader loaded = accessor.viaBedrockUtility$getEquipmentModelLoader();
            if (loaded != null) {
                equipmentLoader = loaded;
            }
        }

        //? if >=1.21.9 {
        return new EntityRendererFactory.Context(
                client.getEntityRenderDispatcher(),
                client.getItemModelManager(),
                client.getMapRenderer(),
                client.getBlockRenderManager(),
                client.getResourceManager(),
                client.getLoadedEntityModels(),
                equipmentLoader,
                client.getAtlasManager(),
                client.textRenderer,
                client.getPlayerSkinCache());
        //?} else {
        /*return new EntityRendererFactory.Context(
                client.getEntityRenderDispatcher(),
                client.getItemModelManager(),
                client.getMapRenderer(),
                client.getBlockRenderManager(),
                client.getResourceManager(),
                client.getLoadedEntityModels(),
                equipmentLoader,
                client.textRenderer);
        *///?}
    }
}
