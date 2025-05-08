package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.NotificationToast;
import io.wispforest.gadget.dump.fake.GadgetDynamicRegistriesPacket;
import io.wispforest.gadget.dump.write.PacketDumpWriter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.NetworkState;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientPacketDumper {

    private static final Logger LOGGER = LoggerFactory.getLogger("gadget/PacketDumper");
    public static final Path DUMP_DIR = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("dumps");

    private volatile static PacketDumpWriter WRITER;

    private ClientPacketDumper() {

    }

    public static void start(boolean doToast) {
        try {
            if (!Files.exists(DUMP_DIR))
                Files.createDirectories(DUMP_DIR);

            String filename = Util.getFormattedCurrentTime() + ".gdump";
            WRITER = new PacketDumpWriter(DUMP_DIR.resolve(filename));

            LOGGER.info("Started dumping to {}", filename);

            var handler = MinecraftClient.getInstance().getNetworkHandler();
            if (handler != null) {
                dump(GadgetDynamicRegistriesPacket.fromRegistries(handler.getRegistryManager()), ConfigurationStates.S2C);
            }

            if (doToast)
                new NotificationToast(
                    Text.translatable("message.gadget.dump.started"),
                    null
                ).register();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        PacketDumpWriter dumper = WRITER;

        if (dumper == null) return;

        synchronized (dumper) {
            if (dumper.isClosed()) return;

            try {
                LOGGER.info("Saved dump to {}", dumper.path());

                new NotificationToast(
                    Text.translatable("message.gadget.dump.stopped"),
                    Text.literal(dumper.path().getFileName().toString())
                )
                    .register();

                dumper.close();
                WRITER = null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void dump(Packet<?> packet, NetworkState<?> state) {
        PacketDumpWriter writer = WRITER;

        if (writer == null) return;

        if (packet instanceof ChunkDataS2CPacket && Gadget.CONFIG.dropChunkData())
            return;

        writer.write(packet, state);
    }

    public static void flushIfNeeded() {
        PacketDumpWriter writer = WRITER;

        if (writer == null) return;

        writer.flush();
        LOGGER.info("Flushing packet dump");
    }

    public static boolean isDumping() {
        return WRITER != null;
    }
}
