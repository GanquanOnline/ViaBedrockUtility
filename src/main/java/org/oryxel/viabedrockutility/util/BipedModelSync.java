package org.oryxel.viabedrockutility.util;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;

public final class BipedModelSync {
    private BipedModelSync() {
    }

    public static boolean isCustomPlayerModel(final BipedEntityModel<?> model) {
        return ((IModelPart) (Object) model.getRootPart()).viaBedrockUtility$isVBUModel();
    }

    public static void copyPose(final BipedEntityModel<?> from, final BipedEntityModel<?> to) {
        copyPose(from.head, to.head);
        copyPose(from.hat, to.hat);
        copyPose(from.body, to.body);
        copyPose(from.leftArm, to.leftArm);
        copyPose(from.rightArm, to.rightArm);
        copyPose(from.leftLeg, to.leftLeg);
        copyPose(from.rightLeg, to.rightLeg);
    }

    private static void copyPose(final ModelPart from, final ModelPart to) {
        if (from == null || to == null) {
            return;
        }
        to.pitch = from.pitch;
        to.yaw = from.yaw;
        to.roll = from.roll;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }
}
