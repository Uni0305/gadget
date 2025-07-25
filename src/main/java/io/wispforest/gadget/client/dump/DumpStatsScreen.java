package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.gui.braid.BraidScreenWithParent;
import io.wispforest.gadget.client.gui.braid.Sidebar;
import io.wispforest.gadget.client.gui.braid.SidebarButton;
import io.wispforest.gadget.client.gui.braid.VanillaTranslucent;
import io.wispforest.gadget.dump.read.PacketDumpReader;
import io.wispforest.gadget.dump.read.SearchTextData;
import io.wispforest.gadget.util.NumberUtil;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import io.wispforest.owo.braid.widgets.scroll.VerticallyScrollable;
import io.wispforest.owo.braid.widgets.stack.Stack;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class DumpStatsScreen extends BraidScreenWithParent {
    public DumpStatsScreen(Screen parent, PacketDumpReader reader, ProgressToast toast) {
        super(parent, new DumpStatsWidget(reader, toast));
    }

    public static class DumpStatsWidget extends StatelessWidget {
        private final Map<String, PacketTypeData> packetTypes = new HashMap<>();
        private int totalSize = 0;

        private final PacketDumpReader reader;

        public DumpStatsWidget( PacketDumpReader reader, ProgressToast toast) {
            this.reader = reader;

            MutableLong progress = new MutableLong(0);
            toast.followProgress(progress::getValue, reader.packets().size());
            for (var packet : reader.packets()) {
                var type = packetTypes.computeIfAbsent(packet.get(SearchTextData.KEY).searchText(), unused -> new PacketTypeData());
                type.total += 1;
                type.size += packet.size();
                totalSize += packet.size();

                progress.add(1);
            }
        }

        @Override
        public Widget build(BuildContext context) {
            return new VanillaTranslucent(
                new Stack(
                    new VerticallyScrollable(
                        null,
                        new Padding(
                            Insets.both(20, 15),
                            new Align(
                                Alignment.TOP_LEFT,
                                new Column(
                                    packetTypes
                                        .entrySet()
                                        .stream()
                                        .sorted(Comparator.comparing(x -> -x.getValue().size))
                                        .map(x -> {
                                            double sizePercent = (double) x.getValue().size / totalSize;
                                            double totalPercent = (double) x.getValue().total / reader.packets().size();

                                            MutableText total = Text.literal(x.getKey())
                                                .append(Text.literal(" " + x.getValue().total + " packets,")
                                                    .formatted(Formatting.GRAY))
                                                .append(Text.literal(" " + NumberUtil.formatFileSize(x.getValue().size) + " total")
                                                    .formatted(Formatting.GRAY))
                                                .append(Text.literal("\n  " + NumberUtil.formatPercent(sizePercent) + " of size"))
                                                .append(Text.literal("\n  " + NumberUtil.formatPercent(totalPercent) + " of packets"));

                                            return new Padding(
                                                Insets.bottom(3),
                                                new Label(
                                                    new LabelStyle(
                                                        Alignment.LEFT,
                                                        null,
                                                        null,
                                                        null
                                                    ),
                                                    true,
                                                    total
                                                )
                                            );
                                        })
                                        .toList()
                                )
                            )
                        )
                    ),
                    new Sidebar(
                        new SidebarButton("text.gadget.back", () -> BraidScreen.maybeOf(context).close())
                    )
                )
            );
        }

        private static class PacketTypeData {
            private int total;
            private int size;
        }
    }
}
