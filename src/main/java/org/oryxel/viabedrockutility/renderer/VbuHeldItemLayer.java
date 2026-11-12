package org.oryxel.viabedrockutility.renderer;

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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.client.model.ModelPart;
import org.joml.Vector3f;
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;

/**
 * Draws held items for Bedrock geometry player models.
 * <p>
 * {@code ModelPart.applyTransform} translates by the part origin and never translates back, so the
 * origin is the anchor every held item hangs from. Vanilla arms put that anchor on the shoulder,
 * while a Bedrock geometry arm keeps it at the model origin, which leaves items floating at the hip.
 * This layer anchors the arm on its Bedrock pivot for the duration of the item draw only, so the
 * arm geometry keeps its own placement.
 */
public class VbuHeldItemLayer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
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
        applyArmAnchor(model, arm, matrices);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0F));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
        final boolean left = arm == Arm.LEFT;
        matrices.translate((left ? -1.0F : 1.0F) / 16.0F, 0.125F, -0.625F);
    }

    private static void applyArmAnchor(PlayerEntityModel model, Arm arm, MatrixStack matrices) {
        final ModelPart part = model.getArm(arm);
        final IModelPart vbu = (IModelPart) (Object) part;
        final Vector3f pivot = vbu.viaBedrockUtility$getPivot();
        final float originX = part.originX;
        final float originY = part.originY;
        final float originZ = part.originZ;

        part.originX = pivot.x;
        part.originY = pivot.y;
        part.originZ = pivot.z;
        vbu.viaBedrockUtility$setNeededOffset(false);
        try {
            part.applyTransform(matrices);
        } finally {
            part.originX = originX;
            part.originY = originY;
            part.originZ = originZ;
            vbu.viaBedrockUtility$setNeededOffset(true);
        }
    }
}
