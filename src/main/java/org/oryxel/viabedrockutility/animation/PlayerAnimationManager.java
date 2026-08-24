package org.oryxel.viabedrockutility.animation;

import net.easecation.bedrockmotion.animation.vanilla.AnimationHelper;
import net.easecation.bedrockmotion.pack.definitions.AnimationDefinitions;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.joml.Vector3f;
import org.oryxel.viabedrockutility.adapter.McBoneModel;
import org.oryxel.viabedrockutility.mixin.interfaces.IModelPart;
import org.oryxel.viabedrockutility.payload.handler.CustomEntityPayloadHandler;
import team.unnamed.mocha.runtime.Scope;
import team.unnamed.mocha.runtime.value.MutableObjectBinding;
import team.unnamed.mocha.runtime.value.Value;

import java.util.*;

public class PlayerAnimationManager {
    /**
     * Specifies which vanilla rotation axes to clear on a bone.
     * Vanilla setAngles() sets pitch(X)/yaw(Y)/roll(Z); we only clear the specific axes
     * that correspond to the vanilla animation being overridden.
     */
    private record BoneClear(String boneName, boolean pitch, boolean yaw, boolean roll) {
    }

    /**
     * Maps animation short names to precise per-bone, per-axis clearing rules.
     * Only axes that vanilla setAngles() actually writes for that animation are cleared.
     * Unlisted animation names are purely additive (no clearing).
     *
     * Vanilla PlayerEntityModel.setAngles() reference:
     *   bob         → arms roll (Z-axis oscillation: cos(age) * amplitude)
     *   move.arms   → arms pitch (X-axis swing: cos(limbSwing) * amplitude)
     *   move.legs   → legs pitch (X-axis swing: cos(limbSwing) * amplitude)
     *   attack.*    → rightArm pitch (X-axis swing)
     *   riding.*    → pitch on arms/legs
     *   sneaking    → body pitch + leg/arm pitch adjustments
     */
    private static final Map<String, List<BoneClear>> VANILLA_CLEAR_MAP = Map.ofEntries(
            Map.entry("bob", List.of(
                    new BoneClear("rightarm", false, false, true),
                    new BoneClear("leftarm", false, false, true)
            )),
            Map.entry("move.arms", List.of(
                    new BoneClear("rightarm", true, false, false),
                    new BoneClear("leftarm", true, false, false)
            )),
            Map.entry("move.legs", List.of(
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false)
            )),
            Map.entry("attack.rotations", List.of(
                    new BoneClear("rightarm", true, false, false)
            )),
            Map.entry("riding.arms", List.of(
                    new BoneClear("rightarm", true, false, false),
                    new BoneClear("leftarm", true, false, false)
            )),
            Map.entry("riding.legs", List.of(
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false)
            )),
            Map.entry("sneaking", List.of(
                    new BoneClear("body", true, false, false)
            )),
            Map.entry("swimming", List.of(
                    new BoneClear("rightarm", true, true, true),
                    new BoneClear("leftarm", true, true, true),
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false),
                    new BoneClear("body", true, false, false)
            )),
            Map.entry("swimming.legs", List.of(
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false)
            )),
            Map.entry("crawling", List.of(
                    new BoneClear("rightarm", true, true, true),
                    new BoneClear("leftarm", true, true, true),
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false),
                    new BoneClear("body", true, false, false)
            )),
            Map.entry("crawling.legs", List.of(
                    new BoneClear("rightleg", true, false, false),
                    new BoneClear("leftleg", true, false, false)
            ))
    );

    private final Map<String, AnimationDefinitions.AnimationData> animations = new LinkedHashMap<>();
    private final Map<String, Long> startTimeMS = new HashMap<>();
    private final Set<String> affectedBones = new HashSet<>();
    private final Vector3f tempVec = new Vector3f();

    public void addAnimation(String shortName, AnimationDefinitions.AnimationData data) {
        animations.put(shortName, data);
        startTimeMS.put(shortName, System.currentTimeMillis());
        affectedBones.addAll(data.compiled().boneAnimations().keySet());
    }

    public boolean isEmpty() {
        return animations.isEmpty();
    }

    public Set<String> getAffectedBones() {
        return affectedBones;
    }

    /**
     * Called every frame from PlayerEntityModel.setAngles() TAIL injection.
     * For known vanilla-replacing animations, clears only the specific axes that vanilla sets.
     * All other animations are purely additive via IModelPart.rotation.
     */
    @SuppressWarnings("unchecked")
    public void animate(Model model, PlayerEntityRenderState state) {
        List<ModelPart> parts = (List<ModelPart>) model.getParts();

        // Reset affected bones' VBU rotation/offset to default before additive blending
        for (String boneName : affectedBones) {
            getPartByName(parts, boneName)
                .ifPresent(part -> ((IModelPart)((Object)part)).viaBedrockUtility$resetToDefaultPose());
        }

        final McBoneModel boneModel = new McBoneModel(model);
        final Scope scope = buildScope(state);
        final long now = System.currentTimeMillis();
        final Iterator<Map.Entry<String, AnimationDefinitions.AnimationData>> iterator = animations.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, AnimationDefinitions.AnimationData> entry = iterator.next();
            final long startedAt = startTimeMS.getOrDefault(entry.getKey(), now);
            final long elapsed = Math.max(0L, now - startedAt);
            final List<BoneClear> clears = VANILLA_CLEAR_MAP.get(entry.getKey());
            if (clears != null) {
                for (BoneClear bc : clears) {
                    Optional<ModelPart> opt = getPartByName(parts, bc.boneName());
                    opt.ifPresent(part -> {
                        if (bc.pitch()) part.pitch = 0;
                        if (bc.yaw()) part.yaw = 0;
                        if (bc.roll()) part.roll = 0;
                    });
                }
            }
            AnimationHelper.animate(scope, boneModel, entry.getValue().compiled(), elapsed, 1.0f, tempVec, null);
            if (isFinished(entry.getValue(), elapsed)) {
                iterator.remove();
                startTimeMS.remove(entry.getKey());
            }
        }
        rebuildAffectedBones();
    }

    static boolean isFinished(AnimationDefinitions.AnimationData data, long elapsedMs) {
        if (data == null || data.compiled() == null) {
            return true;
        }
        if (data.compiled().looping()) {
            return false;
        }
        final float length = data.compiled().lengthInSeconds();
        if (length <= 0f) {
            return false;
        }
        return elapsedMs >= (long) (length * 1000f);
    }

    private void rebuildAffectedBones() {
        affectedBones.clear();
        for (AnimationDefinitions.AnimationData data : animations.values()) {
            affectedBones.addAll(data.compiled().boneAnimations().keySet());
        }
    }

    private Scope buildScope(PlayerEntityRenderState state) {
        // Start from BASE_SCOPE (contains math binding) to get math.cos, math.sin, etc.
        final Scope scope = CustomEntityPayloadHandler.BASE_SCOPE.copy();

        final MutableObjectBinding query = new MutableObjectBinding();
        query.set("life_time", Value.of(state.age / 20.0f));
        query.set("modified_distance_moved", Value.of(state.limbSwingAnimationProgress));
        query.set("modified_move_speed", Value.of(state.limbSwingAmplitude));
        query.set("is_on_ground", Value.of(true));
        query.set("is_alive", Value.of(true));

        scope.set("query", query);
        scope.set("q", query);

        return scope;
    }

    /**
     * Finds a ModelPart by name from a flat list of parts (MC-specific helper).
     */
    private static Optional<ModelPart> getPartByName(List<ModelPart> parts, String name) {
        for (ModelPart part : parts) {
            if (((IModelPart)((Object)part)).viaBedrockUtility$getName().equalsIgnoreCase(name) && part.isEmpty()) {
                return Optional.of(part);
            }
        }
        return Optional.empty();
    }
}
