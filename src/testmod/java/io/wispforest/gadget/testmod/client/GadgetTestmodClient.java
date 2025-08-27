package io.wispforest.gadget.testmod.client;

import io.wispforest.gadget.client.gui.NotificationToast;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.serialization.CodecUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class GadgetTestmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("gadget-testmod")
                .then(literal("epic")
                    .executes(ctx -> {
                        ClientPlayNetworking.send(new EpicPacket("cringe"));
                        return 1;
                    }))
                .then(literal("test-notification")
                    .executes(ctx -> {
                        // Test notification toast
                        new NotificationToast(
                            Text.literal("Test Notification"),
                            Text.literal("This is a test message")
                        ).register();
                        return 1;
                    }))
                .then(literal("test-progress")
                    .executes(ctx -> {
                        // Test progress toast
                        ProgressToast toast = ProgressToast.create(Text.literal("Test Progress"));
                        toast.step(Text.literal("Starting test..."));
                        
                        // Simulate some progress
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                toast.step(Text.literal("Step 1"));
                                Thread.sleep(1000);
                                toast.step(Text.literal("Step 2"));
                                Thread.sleep(1000);
                                toast.finish(Text.literal("Completed!"), false);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                        
                        return 1;
                    })));
        });

        PayloadTypeRegistry.playC2S().register(EpicPacket.ID, CodecUtils.toPacketCodec(EpicPacket.ENDEC));
        ServerPlayNetworking.registerGlobalReceiver(EpicPacket.ID, (pkt, ctx) -> {
            // Do nothing.
        });
    }
}
