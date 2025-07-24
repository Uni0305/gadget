package io.wispforest.gadget.client.gui.braid;

import io.wispforest.owo.braid.core.BraidScreen;
import io.wispforest.owo.braid.framework.widget.Widget;
import net.minecraft.client.gui.screen.Screen;

public class BraidScreenWithParent extends BraidScreen {
    private final Screen parent;

    public BraidScreenWithParent(Screen parent, Widget rootWidget) {
        super(rootWidget);
        this.parent = parent;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
