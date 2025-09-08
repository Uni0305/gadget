package io.wispforest.gadget.dump.fake;

import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.network.ClientDynamicRegistryType;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record GadgetDynamicRegistriesPacket(
        Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> registries,
        Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> tags) implements FakeGadgetPacket {
    public static final int ID = -3;

    private static final PacketCodec<ByteBuf, RegistryKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = Identifier.PACKET_CODEC
        .xmap(RegistryKey::ofRegistry, RegistryKey::getValue);

    // @formatter:off
    public static final PacketCodec<PacketByteBuf, GadgetDynamicRegistriesPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.<ByteBuf, RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>, Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>>>map(
                    HashMap::new,
                    REGISTRY_KEY_CODEC,
                    SerializableRegistries.SerializedRegistryEntry.PACKET_CODEC.collect(PacketCodecs.toList())
            ),
            GadgetDynamicRegistriesPacket::registries,
            PacketCodecs.map(
                    HashMap::new,
                    REGISTRY_KEY_CODEC,
                    PacketCodec.of(TagPacketSerializer.Serialized::writeBuf, TagPacketSerializer.Serialized::fromBuf)
            ),
            GadgetDynamicRegistriesPacket::tags,
            GadgetDynamicRegistriesPacket::new
    );
    // @formatter:on

    public static GadgetDynamicRegistriesPacket fromRegistries(DynamicRegistryManager registries) {
        DynamicOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);
        Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> map = new HashMap<>();

        SerializableRegistries.forEachSyncedRegistry(
            dynamicOps,
            registries,
            Set.of(),
            map::put
        );
        Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> tagMap = registries
                .streamAllRegistries()
                .map(registry -> Pair.of(registry.key(), TagPacketSerializer.serializeTags(registry.value())))
                .filter(pair -> !pair.second().isEmpty())
                .collect(Collectors.toMap(Pair::left, Pair::right));

        return new GadgetDynamicRegistriesPacket(map, tagMap);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public PacketCodec<PacketByteBuf, GadgetDynamicRegistriesPacket> codec() {
        return CODEC;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }
}
