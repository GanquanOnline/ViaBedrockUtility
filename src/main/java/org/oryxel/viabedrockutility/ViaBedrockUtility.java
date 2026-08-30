package org.oryxel.viabedrockutility;

import com.mojang.brigadier.Command;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.command.CommandManager;
import org.oryxel.viabedrockutility.mappings.BedrockMappings;
import org.oryxel.viabedrockutility.material.VanillaMaterials;
import net.easecation.bedrockmotion.pack.PackManager;
import org.oryxel.viabedrockutility.payload.BasePayload;
import org.oryxel.viabedrockutility.payload.handler.CustomEntityPayloadHandler;
import org.oryxel.viabedrockutility.payload.impl.camera.CameraPayload;
import org.oryxel.viabedrockutility.payload.impl.camera.CameraPayloadHandler;

@Getter
@Setter
public class ViaBedrockUtility {
    public static boolean DEBUGGING = true;

    @Getter
    private static final ViaBedrockUtility instance = new ViaBedrockUtility();

    private ViaBedrockUtility() {}

    private CustomEntityPayloadHandler payloadHandler;
    private CameraPayloadHandler cameraPayloadHandler;
    private PackManager packManager;
    private boolean viaBedrockPresent;
    private boolean spectatorNoclip;

    public void init() {
        VanillaMaterials.init();
        BedrockMappings.load();

        // Register custom payload.
        this.payloadHandler = new CustomEntityPayloadHandler();
        PayloadTypeRegistry.configurationS2C().register(BasePayload.ID, BasePayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(BasePayload.ID, BasePayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(BasePayload.ID, (payload, context) -> payload.handle(this.payloadHandler));

        // Register BECamera payload channel (CONFIGURATION for CONFIRM, PLAY for data).
        this.cameraPayloadHandler = new CameraPayloadHandler();
        PayloadTypeRegistry.configurationS2C().register(CameraPayload.ID, CameraPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(CameraPayload.ID, CameraPayload.STREAM_CODEC);
        ClientPlayNetworking.registerGlobalReceiver(CameraPayload.ID, (payload, context) -> payload.handle(this.cameraPayloadHandler));

        // Tick animation overlays on all cached player renderers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.payloadHandler != null) {
                this.payloadHandler.tickAnimationOverlays();
            }
        });

        // To enable debugging in order to use animate test thingy (look at ClientPlayNetworkHandler)
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("vbudebug").executes(context -> {
                            DEBUGGING = !DEBUGGING;
                            System.out.println("Debugging status: " + DEBUGGING);
                            return Command.SINGLE_SUCCESS;
                        }
                )));
    }
}
