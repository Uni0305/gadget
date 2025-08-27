package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;

public class NotificationToast implements Toast {
    private final OwoUIAdapter<FlowLayout> adapter;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Text headText;
    private final Text messageText;

    private Visibility visibility = Visibility.HIDE;

    public NotificationToast(Text headText, Text messageText) {
        OwoUIAdapter<FlowLayout> adapter;
        try {
            adapter = OwoUIAdapter.createWithoutScreen(0, 0, 160, 32, Containers::verticalFlow);

            var root = adapter.rootComponent;

            root
                .child(Components.label(headText)
                    .maxWidth(160)
                    .horizontalTextAlignment(HorizontalAlignment.CENTER))
                .surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0xFF5800FF)))
                .allowOverflow(true)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER)
                .padding(Insets.of(5));

            if (messageText != null)
                root.child(Components.label(messageText));

            adapter.inflateAndMount();
        } catch (Exception e) {
            // If adapter creation fails, set to null for fallback rendering
            adapter = null;
        }
        
        // Store texts for fallback rendering
        this.adapter = adapter;
        this.headText = headText;
        this.messageText = messageText;
    }

    public void register() {
        if (!client.isOnThread()) {
            client.execute(this::register);
            return;
        }

        // Always register, even if adapter failed - fallback rendering will handle it
        client.getToastManager().add(this);
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        // Ensure the adapter and its components are properly initialized
        if (adapter != null && adapter.rootComponent != null) {
            this.adapter.render(context, 0, 0, client.getRenderTickCounter().getTickProgress(false));
        } else {
            // Fallback rendering when adapter is not available
            drawFallback(context, textRenderer);
        }
    }
    
    private void drawFallback(DrawContext context, TextRenderer textRenderer) {
        // Draw background and border
        context.fill(0, 0, 160, 32, 0xBF000000);
        context.drawBorder(0, 0, 160, 32, 0xFF5800FF);
        
        // Draw head text
        if (headText != null) {
            String headTextStr = headText.getString();
            int headTextWidth = textRenderer.getWidth(headTextStr);
            int headTextX = (160 - headTextWidth) / 2;
            int yPos = messageText != null ? 6 : 12; // Adjust position based on whether we have message text
            context.drawText(textRenderer, headText, headTextX, yPos, 0xFFFFFF, false);
        }
        
        // Draw message text
        if (messageText != null) {
            String messageTextStr = messageText.getString();
            int messageTextWidth = textRenderer.getWidth(messageTextStr);
            int messageTextX = (160 - messageTextWidth) / 2;
            context.drawText(textRenderer, messageText, messageTextX, 18, 0xFFFFFF, false);
        }
    }

    @Override
    public void update(ToastManager manager, long time) {
        this.visibility = time > 5000 ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public Visibility getVisibility() {
        return this.visibility;
    }
}
