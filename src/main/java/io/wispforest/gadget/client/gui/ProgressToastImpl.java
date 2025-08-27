package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

import java.util.function.LongSupplier;

public class ProgressToastImpl implements Toast, ProgressToast {
    private OwoUIAdapter<FlowLayout> adapter;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean attached = false;
    private final Text headText;

    private LabelComponent stepLabel;
    private BoxComponent progressBox;
    private long stopTime = 0;
    private LongSupplier following = null;
    private long followingTotal = 0;
    private Text currentStepText = Text.empty();

    private Visibility visibility = Visibility.HIDE;

    public ProgressToastImpl(Text headText) {
        this.headText = headText;
        try {
            this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, 160, 32, Containers::verticalFlow);

            var root = this.adapter.rootComponent;

            root
                .child(Components.label(headText)
                    .maxWidth(160)
                    .horizontalTextAlignment(HorizontalAlignment.CENTER)
                    .color(Color.WHITE)
                    .margins(Insets.bottom(0)))
                .child(stepLabel = Components.label(Text.empty())
                    .maxWidth(160)
                    .horizontalTextAlignment(HorizontalAlignment.CENTER)
                    .color(Color.WHITE))
                .child((progressBox = Components.box(Sizing.fixed(0), Sizing.fixed(3)))
                    .color(Color.WHITE)
                    .fill(true)
                    .positioning(Positioning.absolute(0, 15)))
                .surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0xFF5800FF)))
                .allowOverflow(true)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(10));

            this.adapter.inflateAndMount();
        } catch (Exception e) {
            // If adapter creation fails, set to null to use fallback rendering
            this.adapter = null;
            this.stepLabel = null;
            this.progressBox = null;
        }
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        long value = following == null ? -1 : following.getAsLong();

        // Draw background and border
        context.fill(0, 0, 160, 32, 0xBF000000);
        context.drawBorder(0, 0, 160, 32, 0xFF5800FF);

        // Draw progress bar
        int progressWidth = value < 0 ? 0 : (int) (value * 140 / followingTotal);
        context.fill(10, 25, 10 + progressWidth, 28, Color.WHITE.argb());

        // Draw head text (try from adapter first, then fallback)
        Text headToRender = headText;
        if (adapter != null && adapter.rootComponent != null && !adapter.rootComponent.children().isEmpty()) {
            try {
                LabelComponent headLabel = (LabelComponent) adapter.rootComponent.children().getFirst();
                if (headLabel != null && headLabel.text() != null) {
                    headToRender = headLabel.text();
                }
            } catch (Exception e) {
                // Use stored headText as fallback
            }
        }
        
        if (headToRender != null) {
            String headTextStr = headToRender.getString();
            int headTextWidth = textRenderer.getWidth(headTextStr);
            int headTextX = (160 - headTextWidth) / 2;
            context.drawText(textRenderer, headToRender, headTextX, 8, Color.WHITE.argb(), false);
        }
        
        // Draw step text (try from stepLabel first, then fallback)
        Text stepToRender = currentStepText;
        if (stepLabel != null && stepLabel.text() != null && !stepLabel.text().getString().isEmpty()) {
            stepToRender = stepLabel.text();
        }
        
        if (stepToRender != null && !stepToRender.getString().isEmpty()) {
            String stepTextStr = stepToRender.getString();
            int stepTextWidth = textRenderer.getWidth(stepTextStr);
            int stepTextX = (160 - stepTextWidth) / 2;
            context.drawText(textRenderer, stepToRender, stepTextX, 16, Color.WHITE.argb(), false);
        }
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (stopTime == -1) {
            stopTime = time + 1;
        }  else if (stopTime == -2) {
            this.visibility = Visibility.HIDE;
            return;
        }

        if (stopTime == 0) {
            this.visibility = Visibility.SHOW;
        } else {
            this.visibility = time - stopTime > 2500 ? Visibility.HIDE : Visibility.SHOW;
        }
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }

    @Override
    public void step(Text text) {
        MinecraftClient.getInstance().execute(() -> {
            this.currentStepText = text; // Store for fallback rendering
            
            if (!attached) {
                MinecraftClient.getInstance().getToastManager().add(this);
                attached = true;
            }

            // Safely update step label
            if (this.stepLabel != null) {
                this.stepLabel.text(text);
            }
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
            this.currentStepText = text; // Store for fallback rendering
            if (this.stepLabel != null) {
                this.stepLabel.text(text);
            }
            this.following = null;
            stopTime = hideImmediately ? -2 : -1;
        });
    }

    public void oom(OutOfMemoryError oom) {
        adapter.rootComponent.clearChildren();
        client.currentScreen.removed();
        client.currentScreen = null;

        following = null;
        adapter = null;
        stepLabel = null;
        progressBox = null;

        client.execute(() -> {
            client.getToastManager().clear();

            throw oom;
        });
    }
}
