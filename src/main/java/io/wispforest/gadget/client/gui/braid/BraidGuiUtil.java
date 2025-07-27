package io.wispforest.gadget.client.gui.braid;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ThrowableUtil;
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

import java.util.ArrayList;
import java.util.List;

public final class BraidGuiUtil {
    private BraidGuiUtil() {

    }

    public static Widget codeListing(String all) {
        var lines = all.lines().toList();

        int i = 1;

        List<Widget> widgets = new ArrayList<>();

        for (String line : lines) {
            var text = Text.literal("")
                .append(Text.literal(line.replace("\t", "    ")));

            widgets.add(new Align(
                Alignment.RIGHT,
                new Label(
                    Text.literal(i + " ")
                        .formatted(Formatting.GRAY)
                )
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

    public static Label showException(Throwable e) {
        String fullExceptionText = ThrowableUtil.throwableToString(e);
        return new Label(
            Text.literal(fullExceptionText.replace("\t", "    "))
                .formatted(Formatting.RED));
    }

}
