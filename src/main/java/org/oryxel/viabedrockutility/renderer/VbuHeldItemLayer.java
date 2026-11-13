package org.oryxel.viabedrockutility.renderer;

import net.easecation.bedrockmotion.util.MathUtil;
import net.minecraft.client.render.OverlayTexture;
//? if >=1.21.9 {
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
//?} else {
/*import net.minecraft.client.render.VertexConsumerProvider;
*///?}
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;

import java.util.Locale;
import java.util.Map;

/**
 * Draws held items for Bedrock geometry player models.
 * <p>
 * {@code HeldItemFeatureRendererMixin} drops the vanilla layer for Bedrock models because the vanilla
 * anchor assumes {@code right_arm}/{@code left_arm} exist. Bedrock geometries name their arm bones
 * themselves, so this layer resolves the real arm bone, anchors at its pivot (the shoulder) and then
 * applies the animation and vanilla rotations there, which keeps the item glued to the hand.
 */
public class VbuHeldItemLayer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final float VANILLA_ARM_PIVOT_X = 5.0F;
    private static final float VANILLA_ARM_PIVOT_Y = 2.0F;
    private static final int MAX_BONE_DEPTH = 64;

    private final Quaternionf tempQuaternion = new Quaternionf();

    public VbuHeldItemLayer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context) {
        super(context);
    }

    //? if >=1.21.9 {
    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        renderHand(matrices, queue, light, state, Arm.RIGHT, state.rightHandItemState);
        renderHand(matrices, queue, light, state, Arm.LEFT, state.leftHandItemState);
    }

    private void renderHand(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, PlayerEntityRenderState state, Arm arm, ItemRenderState itemState) {
        if (itemState.isEmpty()) {
            return;
        }
        matrices.push();
        renderAnchored(matrices, state, arm);
        itemState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
        matrices.pop();
    }
    //?} else {
    /*@Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
        renderHand(matrices, vertexConsumers, light, state, Arm.RIGHT, state.rightHandItemState);
        renderHand(matrices, vertexConsumers, light, state, Arm.LEFT, state.leftHandItemState);
    }

    private void renderHand(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, Arm arm, ItemRenderState itemState) {
        if (itemState.isEmpty()) {
            return;
        }
        matrices.push();
        renderAnchored(matrices, state, arm);
        itemState.render(matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, state.outlineColor);
        matrices.pop();
    }
    *///?}

    private void renderAnchored(MatrixStack matrices, PlayerEntityRenderState state, Arm arm) {
        final PlayerEntityModel model = this.getContextModel();
        model.getRootPart().applyTransform(matrices);

        final ModelPart armPart = resolveArmPart(model, arm);
        if (armPart == null) {
            // No arm bone in this geometry: fall back to the vanilla shoulder so the item still
            // sits at the hand instead of the chest.
            matrices.translate((arm == Arm.LEFT ? VANILLA_ARM_PIVOT_X : -VANILLA_ARM_PIVOT_X) / 16.0F, VANILLA_ARM_PIVOT_Y / 16.0F, 0.0F);
        } else if (!((IModelPart) (Object) armPart).viaBedrockUtility$isVBUModel()) {
            // Plain vanilla model: origin, rotation and scale already follow vanilla semantics.
            armPart.applyTransform(matrices);
        } else {
            final Vector3f anchor = ((IModelPart) (Object) armPart).viaBedrockUtility$getPivot();
            matrices.translate(anchor.x / 16.0F, anchor.y / 16.0F, anchor.z / 16.0F);
            applyAnimationRotation(matrices, armPart);
            applyVanillaRotation(matrices, armPart);
        }

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        final boolean left = arm == Arm.LEFT;
        matrices.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
    }

    private void applyAnimationRotation(MatrixStack matrices, ModelPart part) {
        final Vector3f rotation = ((IModelPart) (Object) part).viaBedrockUtility$getRotation();
        if (rotation.x == 0.0F && rotation.y == 0.0F && rotation.z == 0.0F) {
            return;
        }
        matrices.multiply(this.tempQuaternion.rotationZYX(
                rotation.z * MathUtil.DEGREES_TO_RADIANS,
                rotation.y * MathUtil.DEGREES_TO_RADIANS,
                rotation.x * MathUtil.DEGREES_TO_RADIANS));
    }

    private void applyVanillaRotation(MatrixStack matrices, ModelPart part) {
        if (part.pitch == 0.0F && part.yaw == 0.0F && part.roll == 0.0F) {
            return;
        }
        matrices.multiply(this.tempQuaternion.rotationZYX(part.roll, part.yaw, part.pitch));
    }

    private ModelPart resolveArmPart(PlayerEntityModel model, Arm arm) {
        final ModelPart direct = arm == Arm.LEFT ? model.leftArm : model.rightArm;
        if (isRealBone(direct)) {
            return direct;
        }
        final String side = arm == Arm.LEFT ? "left" : "right";
        ModelPart found = findArmBone(model.getRootPart(), side, true, 0);
        if (found == null) {
            found = findArmBone(model.getRootPart(), side, false, 0);
        }
        return found;
    }

    /**
     * A part returned by {@code getChild} for a missing bone is an empty placeholder: it has no cuboids
     * and no children, so anchoring on it would drop the item at the model origin.
     */
    private static boolean isRealBone(ModelPart part) {
        final IModelPart vbu = (IModelPart) (Object) part;
        return !vbu.viaBedrockUtility$getCuboids().isEmpty() || !vbu.viaBedrockUtility$getChildren().isEmpty();
    }

    private static ModelPart findArmBone(ModelPart parent, String side, boolean exact, int depth) {
        if (depth > MAX_BONE_DEPTH) {
            return null;
        }
        for (Map.Entry<String, ModelPart> entry : ((IModelPart) (Object) parent).viaBedrockUtility$getChildren().entrySet()) {
            final ModelPart child = entry.getValue();
            if (isArmName(entry.getKey(), side, exact)) {
                return child;
            }
            final ModelPart nested = findArmBone(child, side, exact, depth + 1);
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private static boolean isArmName(String name, String side, boolean exact) {
        final String lower = name.toLowerCase(Locale.ROOT);
        if (lower.contains("sleeve")) {
            return false;
        }
        if (exact) {
            return lower.equals(side + "arm") || lower.equals(side + "_arm");
        }
        return lower.contains("arm") && lower.contains(side);
    }
}
