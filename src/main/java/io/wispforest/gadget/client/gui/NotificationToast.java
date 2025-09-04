package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public class NotificationToast extends BaseGadgetToast.VerticalFlow<NotificationToast> {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public NotificationToast(Text headText, @Nullable Text messageText) {
        super(Duration.ofSeconds(5));

        this.rootComponent.child(Components.label(headText).horizontalTextAlignment(HorizontalAlignment.CENTER));

        if (messageText != null) this.rootComponent.child(Components.label(messageText));

        this.inflateAndMount();
    }

    public void register() {
        if (!client.isOnThread()) {
            client.execute(this::register);
            return;
        }

        client.getToastManager().add(this);
    }
}
