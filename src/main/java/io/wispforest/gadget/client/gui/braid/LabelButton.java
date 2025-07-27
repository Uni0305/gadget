package io.wispforest.gadget.client.gui.braid;

import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.widget.StatelessWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Hoverable;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LabelButton extends StatelessWidget {
    private final Text text;
    private final Runnable onClick;

    public LabelButton(Text text, Runnable onClick) {
        this.text = text;
        this.onClick = onClick;
    }

    @Override
    public Widget build(BuildContext context) {
        return Actions.click(
            w -> w.cursorStyle(CursorStyle.HAND),
            () -> {
                UISounds.playInteractionSound();
                onClick.run();
            },
            new Hoverable(
                new Label(text),
                new Label(text.copy().formatted(Formatting.BLUE))
            )
        );
    }
}
