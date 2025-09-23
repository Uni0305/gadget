package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import io.wispforest.gadget.dump.fake.GadgetDynamicRegistriesPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationNetworkHandler.class)
public abstract class ClientConfigurationNetworkHandlerMixin extends ClientCommonNetworkHandler {
    protected ClientConfigurationNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onReady", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;transitionInbound(Lnet/minecraft/network/state/NetworkState;Lnet/minecraft/network/listener/PacketListener;)V"))
    private void writeRegistries(ReadyS2CPacket packet, CallbackInfo ci, @Local DynamicRegistryManager.Immutable registries) {
        if (!ClientPacketDumper.isDumping()) return;

        ClientPacketDumper.dump(GadgetDynamicRegistriesPacket.fromRegistries(registries), ConfigurationStates.S2C);
    }
}
