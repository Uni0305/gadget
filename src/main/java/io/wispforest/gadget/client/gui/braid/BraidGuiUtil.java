package io.wispforest.gadget.client.gui.braid;

import io.wispforest.gadget.Gadget;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.LayoutAxis;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.grid.Grid;
import io.wispforest.owo.braid.widgets.label.DefaultLabelStyle;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.label.LabelStyle;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class BraidGuiUtil {
    private BraidGuiUtil() {

    }

    public static Widget codeListing(String all) {
        var lines = all.lines().toList();
        int maxWidth = Integer.toString(lines.size() - 1).length();

        int i = 0;

        List<Widget> widgets = new ArrayList<>();

        for (String line : lines) {
            var text = Text.literal("")
                .append(Text.literal(line.replace("\t", "    ")));

            widgets.add(new Label(
                Text.literal(StringUtils.leftPad(Integer.toString(i), maxWidth) + " ")
                    .formatted(Formatting.GRAY)
            ));

            widgets.add(new Label(
                text
            ));

            i++;
        }

        return new DefaultLabelStyle(
            new LabelStyle(Alignment.LEFT, null, Style.EMPTY.withFont(Gadget.id("monocraft")), null),
            new Align(
                Alignment.TOP_LEFT,
                new Grid(
                    LayoutAxis.VERTICAL,
                    2,
                    Grid.CellFit.tight(),
                    widgets
                )
            )
        );
    }
}
