package io.wispforest.gadget.dump.fake;

import io.wispforest.gadget.dump.read.unwrapped.UnprocessedUnwrappedPacket;
import io.wispforest.gadget.dump.read.unwrapped.UnwrappedPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record GadgetReadErrorPacket(byte[] data, int packetId, Exception exception) implements FakeGadgetPacket {
    public static final int ID = -2;

    public static GadgetReadErrorPacket from(PacketByteBuf buf, int packetId, Exception exception) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return new GadgetReadErrorPacket(data, packetId, exception);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public PacketCodec<PacketByteBuf, GadgetReadErrorPacket> codec() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnwrappedPacket unwrapGadget() {
        return new UnprocessedUnwrappedPacket(data);
    }
}
