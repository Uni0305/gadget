package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.LongSupplier;

public class ProgressToastImpl extends BaseGadgetToast.VerticalFlow<ProgressToastImpl> implements ProgressToast {
    private static final int NEVER_STOP = -1;
    private static final int STOP_NOW = -2;
    private static final int STOP_DELAYED = -3;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean attached = false;

    private LabelComponent stepLabel;
    private BoxComponent progressBox;
    private long stopTime = NEVER_STOP;
    private LongSupplier following = null;
    private long followingTotal = 0;

    public ProgressToastImpl(Text headText) {
        super(ProgressToastImpl::isVisible);

        rootComponent
            .child(Components.label(headText)
                    .horizontalTextAlignment(HorizontalAlignment.CENTER)
                    .margins(Insets.bottom(0)))
                .child((stepLabel = Components.label(Text.empty())).horizontalTextAlignment(HorizontalAlignment.CENTER))
            .child((progressBox = Components.box(Sizing.fixed(0), Sizing.fixed(3)))
                    .color(Color.WHITE)
                    .fill(true)
                    .positioning(Positioning.absolute(0, 15)));

        this.inflateAndMount();
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long time) {
        long value = following == null ? -1 : following.getAsLong();

        if (value < 0) {
            progressBox.horizontalSizing(Sizing.fixed(0));
            following = null;
        } else {
            progressBox.horizontalSizing(Sizing.fixed((int) (value * 140 / followingTotal)));
        }

        super.draw(context, textRenderer, time);
    }

    private boolean isVisible(long time) {
        if (stopTime == STOP_DELAYED) {
            stopTime = time + 2500;
            return true;
        } else if (stopTime == STOP_NOW) {
            return false;
        } else if (stopTime == NEVER_STOP) {
            return true;
        } else {
            return time < stopTime;
        }
    }

    @Override
    public void step(Text text) {
        MinecraftClient.getInstance().execute(() -> {
            if (!attached) {
                MinecraftClient.getInstance().getToastManager().add(this);
                attached = true;
            }

            this.stepLabel.text(text);
            this.following = null;
        });
    }

    @Override
    public void followProgress(LongSupplier following, long total) {
        MinecraftClient.getInstance().execute(() -> {
            this.following = following;
            this.followingTotal = total;
        });
    }

    @Override
    public void force() {
        MinecraftClient.getInstance().execute(() -> {
            if (!attached) {
                MinecraftClient.getInstance().getToastManager().add(this);
                attached = true;
            }
        });
    }

    @Override
    public void finish(Text text, boolean hideImmediately) {
        MinecraftClient.getInstance().execute(() -> {
            this.stepLabel.text(text);
            this.following = null;
            System.out.println("Finish");
            stopTime = hideImmediately ? STOP_NOW : STOP_DELAYED;
        });
    }

    public void oom(OutOfMemoryError oom) {
        rootComponent.clearChildren();
        if (client.currentScreen != null) {
            client.currentScreen.removed();
            client.currentScreen = null;
        }

        following = null;
        stepLabel = null;
        progressBox = null;

        client.execute(() -> {
            client.getToastManager().clear();

            throw oom;
        });
    }
}
