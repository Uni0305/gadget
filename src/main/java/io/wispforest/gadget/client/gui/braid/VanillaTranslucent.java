package io.wispforest.gadget.client.gui.braid;

import io.wispforest.owo.braid.framework.instance.SingleChildWidgetInstance;
import io.wispforest.owo.braid.framework.widget.SingleChildInstanceWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.ui.core.OwoUIDrawContext;

public class VanillaTranslucent extends SingleChildInstanceWidget {
    public VanillaTranslucent(Widget child) {
        super(child);
    }

    @Override
    public SingleChildWidgetInstance<?> instantiate() {
        return new Instance(this);
    }

    public static class Instance extends SingleChildWidgetInstance.ShrinkWrap<VanillaTranslucent> {
        public Instance(VanillaTranslucent widget) {
            super(widget);
        }

        @Override
        public void draw(OwoUIDrawContext ctx) {
            ctx.drawGradientRect(
                0, 0, (int) this.transform.width(), (int) this.transform.height(),
                0xC0101010, 0xC0101010, 0xD0101010, 0xD0101010
            );

            super.draw(ctx);
        }
    }
}
