package org.oryxel.viabedrockutility.animation;

import net.easecation.bedrockmotion.animation.vanilla.VBUAnimation;
import net.easecation.bedrockmotion.pack.definitions.AnimationDefinitions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAnimationManagerTest {

    @Test
    void oneShotAnimationFinishesAfterLength() {
        final AnimationDefinitions.AnimationData data = new AnimationDefinitions.AnimationData(
                null,
                VBUAnimation.Builder.create(0.5f).build()
        );
        assertFalse(PlayerAnimationManager.isFinished(data, 0L));
        assertFalse(PlayerAnimationManager.isFinished(data, 499L));
        assertTrue(PlayerAnimationManager.isFinished(data, 500L));
    }

    @Test
    void loopingAnimationNeverFinishes() {
        final AnimationDefinitions.AnimationData data = new AnimationDefinitions.AnimationData(
                null,
                VBUAnimation.Builder.create(0.5f).looping().build()
        );
        assertFalse(PlayerAnimationManager.isFinished(data, 0L));
        assertFalse(PlayerAnimationManager.isFinished(data, 50_000L));
    }

    @Test
    void zeroLengthOneShotKeepsPlaying() {
        final AnimationDefinitions.AnimationData data = new AnimationDefinitions.AnimationData(
                null,
                VBUAnimation.Builder.create(0f).build()
        );
        assertFalse(PlayerAnimationManager.isFinished(data, 10_000L));
    }
}
