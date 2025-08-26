package io.wispforest.gadget.dump.fake;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import io.wispforest.gadget.mixin.TagPacketSerializerAccessor;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record GadgetDynamicRegistriesPacket(
    Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> elements,
    Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> tags
) implements FakeGadgetPacket {
    public static final int ID = -3;

    private static final PacketCodec<ByteBuf, RegistryKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = Identifier.PACKET_CODEC
        .xmap(RegistryKey::ofRegistry, RegistryKey::getValue);

    private static final PacketCodec<PacketByteBuf, TagPacketSerializer.Serialized> TAG_SERIALIZED_CODEC = PacketCodec.of(
        TagPacketSerializer.Serialized::writeBuf,
        TagPacketSerializer.Serialized::fromBuf
    );

    public static final PacketCodec<PacketByteBuf, GadgetDynamicRegistriesPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.map(
            HashMap::new,
            REGISTRY_KEY_CODEC,
            SerializableRegistries.SerializedRegistryEntry.PACKET_CODEC.collect(PacketCodecs.toList())
        ),
        GadgetDynamicRegistriesPacket::elements,
        PacketCodecs.map(
            HashMap::new,
            REGISTRY_KEY_CODEC,
            TAG_SERIALIZED_CODEC
        ),
        GadgetDynamicRegistriesPacket::tags,
        GadgetDynamicRegistriesPacket::new
    );

    public static GadgetDynamicRegistriesPacket fromRegistries(DynamicRegistryManager.Immutable registries) {
        DynamicOps<NbtElement> dynamicOps = registries.getOps(NbtOps.INSTANCE);
        Map<RegistryKey<? extends Registry<?>>, List<SerializableRegistries.SerializedRegistryEntry>> elements = new HashMap<>();

        Map<RegistryKey<? extends Registry<?>>, TagPacketSerializer.Serialized> tags = registries.streamAllRegistries()
            .map(registry -> Pair.of(registry.key(), TagPacketSerializerAccessor.serializeTags(registry.value())))
            .filter(pair -> !pair.getSecond().isEmpty())
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

        SerializableRegistries.forEachSyncedRegistry(
            dynamicOps,
            registries,
            Set.of(),
            elements::put
        );

        return new GadgetDynamicRegistriesPacket(elements, tags);
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
