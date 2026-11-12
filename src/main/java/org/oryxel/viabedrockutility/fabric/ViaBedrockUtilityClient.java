package org.oryxel.viabedrockutility.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.oryxel.viabedrockutility.pack.PackWatermark;

public final class ViaBedrockUtilityClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PackWatermark.register();
    }
}
