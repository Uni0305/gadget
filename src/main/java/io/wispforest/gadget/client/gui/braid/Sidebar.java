package io.wispforest.gadget.client.gui.braid;

import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.flex.Column;

public class Sidebar extends StatelessWidget {
    private final SidebarButton[] buttons;

    public Sidebar(SidebarButton... buttons) {
        this.buttons = buttons;
    }

    @Override
    public Widget build(BuildContext context) {
        return new Align(
            Alignment.TOP_LEFT,
            new Padding(
                Insets.all(2),
                new Column(
                    buttons
                )
            )
        );
    }
}
