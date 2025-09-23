package io.wispforest.gadget.dump.write;

import io.netty.buffer.ByteBuf;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.dump.fake.FakeGadgetPacket;
import io.wispforest.gadget.dump.fake.GadgetDynamicRegistriesPacket;
import io.wispforest.gadget.dump.fake.GadgetReadErrorPacket;
import io.wispforest.gadget.dump.fake.GadgetWriteErrorPacket;
import io.wispforest.gadget.util.SlicingPacketByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.state.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;

public final class PacketDumping {
    private static final Int2ObjectMap<PacketCodec<? super PacketByteBuf, ? extends FakeGadgetPacket>> PACKETS = new Int2ObjectOpenHashMap<>();

    private PacketDumping() {

    }

    static {
        register(GadgetWriteErrorPacket.ID, GadgetWriteErrorPacket.CODEC);
//        register(GadgetReadErrorPacket.ID, GadgetReadErrorPacket.CODEC);
        register(GadgetDynamicRegistriesPacket.ID, GadgetDynamicRegistriesPacket.CODEC);

    }

    public static void register(int id, PacketCodec<? super PacketByteBuf, ? extends FakeGadgetPacket> codec) {
        if (PACKETS.put(id, codec) != null) {
            throw new IllegalStateException("Codec on " + id + " collides with another codec");
        }
    }

    @SuppressWarnings("unchecked")
    public static void writePacket(PacketByteBuf buf, Packet<?> packet, NetworkState<?> state) {
        int startWriteIdx = buf.writerIndex();
        int packetId = 0;

        try {
            if (packet instanceof FakeGadgetPacket fakePacket) {
                packetId = fakePacket.id();
                buf.writeVarInt(packetId);
                ((PacketCodec<ByteBuf, FakeGadgetPacket>) fakePacket.codec()).encode(buf, fakePacket);
                return;
            }

            ((PacketCodec<ByteBuf, Object>)(Object) state.codec()).encode(new SlicingPacketByteBuf(buf), packet);
        } catch (Exception e) {
            buf.writerIndex(startWriteIdx);

            Gadget.LOGGER.error("Error while writing packet {}", packet, e);

            GadgetWriteErrorPacket writeError = GadgetWriteErrorPacket.fromThrowable(packetId, e);
            buf.writeVarInt(writeError.id());
            writeError.codec().encode(buf, writeError);
        }
    }

    public static Packet<?> readPacket(PacketByteBuf buf, NetworkState<?> state) {
        int startOfData = buf.readerIndex();
        int packetId = buf.readVarInt();

        try {
            PacketCodec<? super PacketByteBuf, ? extends FakeGadgetPacket> fakeCodec = PACKETS.get(packetId);
            if (fakeCodec != null) {
                return fakeCodec.decode(buf).unwrapVanilla();
            }

            buf.readerIndex(startOfData);

            return state.codec().decode(buf);
        } catch (Exception e) {
            buf.readerIndex(startOfData);
            return GadgetReadErrorPacket.from(buf, packetId, e);
        }
    }
}
