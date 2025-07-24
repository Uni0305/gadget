package io.wispforest.gadget.client.gui.braid;

import io.wispforest.gadget.client.gui.SidebarBuilder;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.*;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.braid.widgets.stack.Stack;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class SidebarButton extends StatelessWidget {
    private final Text icon;
    private final Text tooltip;
    private final Runnable handler;

    public SidebarButton(Text icon, @Nullable Text tooltip, Runnable handler) {
        this.icon = icon;
        this.tooltip = tooltip;
        this.handler = handler;
    }

    public SidebarButton(String translationKeyBase, Runnable handler) {
        this(Text.translatable(translationKeyBase), Text.translatable(translationKeyBase + ".tooltip"), handler);
    }

    @Override
    public Widget build(BuildContext context) {
        Widget w = new Sized(
            16, 16,
            Actions.click(
                widget -> widget.cursorStyle(CursorStyle.HAND),
                () -> {
                    UISounds.playInteractionSound();
                    handler.run();
                },
                new Stack(
                    new Hoverable(new Padding(Insets.none()), new Box(Color.ofArgb(0x80ffffff))),
                    new Padding(
                        Insets.of(4, 0, 3, 0),
                        new Label(icon)
                    )
                )
            )
        );

        if (tooltip != null) {
            w = new Tooltip(tooltip, w);
        }

        return w;
    }
}
