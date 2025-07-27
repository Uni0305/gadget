package io.wispforest.gadget.client.gui.braid;

import io.wispforest.gadget.Gadget;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.DefaultLabelStyle;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class HexDump extends StatefulWidget {
    private final byte[] bytes;
    private final boolean doEllipsis;

    public HexDump(byte[] bytes, boolean doEllipsis) {
        this.bytes = bytes;
        this.doEllipsis = doEllipsis;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<HexDump> {
        private boolean expanded = false;

        @Override
        public void didUpdateWidget(HexDump oldWidget) {
            this.setState(() -> {
                this.expanded = false;
            });
        }

        @Override
        public Widget build(BuildContext context) {
            List<Widget> children = new ArrayList<>();
            boolean hasEllipsis = false;

            int index = 0;
            while (index < widget().bytes.length) {
                StringBuilder line = new StringBuilder();

                line.append(String.format("%04x  ", index));

                int i;
                for (i = 0; i < 16 && index < widget().bytes.length; i++) {
                    short b = (short) (widget().bytes[index] & 0xff);

                    line.append(String.format("%02x ", b));
                    index++;
                }

                line.append("   ".repeat(Math.max(0, 16 - i)));

                for (int j = 0; j < i; j++) {
                    short b = (short) (widget().bytes[index - i + j] & 0xff);

                    if (b >= 32 && b < 127)
                        line.append((char) b);
                    else
                        line.append('.');
                }

                var label = new Label(Text.literal(line.toString()));

                if (children.size() > 10 && widget().doEllipsis && !this.expanded) {
                    hasEllipsis = true;
                    break;
                } else {
                    children.add(label);
                }
            }

            if (hasEllipsis) {
                children.add(new LabelButton(Text.literal("..."), () -> {
                    setState(() -> {
                        this.expanded = true;
                    });
                }));
            }

            return new DefaultLabelStyle(
                new LabelStyle(Alignment.LEFT, null, Style.EMPTY.withFont(Gadget.id("monocraft")), null),
                new Column(new Padding(Insets.bottom(3)), children)
            );
        }
    }
}
