package io.wispforest.gadget.client.resource;

import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.core.cursor.CursorStyle;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Box;
import io.wispforest.owo.braid.widgets.basic.Hoverable;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.action.Actions;
import io.wispforest.owo.braid.widgets.collapsible.Collapsible;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.flex.CrossAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.MainAxisAlignment;
import io.wispforest.owo.braid.widgets.flex.Row;
import io.wispforest.owo.braid.widgets.label.Label;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class TreeEntryWidget<K> extends StatefulWidget {
    private final TreeEntry<K> entry;
    private final Consumer<K> onClick;

    public TreeEntryWidget(TreeEntry<K> entry, Consumer<K> onClick) {
        this.entry = entry;
        this.onClick = onClick;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public class State extends WidgetState<TreeEntryWidget<K>> {
        private boolean collapsed = true;

        @Override
        public Widget build(BuildContext context) {
            Widget row = new Label(Text.literal(entry.name));

            if (entry.key != null) {
                row = Actions.click(
                    w -> w.cursorStyle(CursorStyle.HAND),
                    () -> {
                        UISounds.playInteractionSound();
                        onClick.accept(entry.key);
                    },
                    row
                );

                row = new Hoverable(
                    row,
                    new Box(Color.ofArgb(0x80ffffff), row)
                );
            }

            row = new Row(
                MainAxisAlignment.START,
                CrossAxisAlignment.START,
                row
            );

            if (entry.children.isEmpty()) {
                return new Padding(
                    Insets.left(5),
                    row
                );
            } else {
                Widget children = new Column(
                    entry
                        .children
                        .values()
                        .stream()
                        .map(x -> new TreeEntryWidget<>(x, onClick))
                        .toList()
                );

                if (entry.name.isEmpty())
                    return children;

                return new Collapsible(
                    true,
                    this.collapsed,
                    c -> this.setState(() -> this.collapsed = c),
                    row,
                    children
                );
            }
        }
    }
}
