package io.wispforest.gadget.client.resource;

import io.wispforest.gadget.Gadget;
import io.wispforest.owo.braid.core.Alignment;
import io.wispforest.owo.braid.core.Insets;
import io.wispforest.owo.braid.framework.BuildContext;
import io.wispforest.owo.braid.framework.proxy.WidgetState;
import io.wispforest.owo.braid.framework.widget.StatefulWidget;
import io.wispforest.owo.braid.framework.widget.Widget;
import io.wispforest.owo.braid.widgets.basic.Align;
import io.wispforest.owo.braid.widgets.basic.Padding;
import io.wispforest.owo.braid.widgets.basic.Sized;
import io.wispforest.owo.braid.widgets.basic.TextureWidget;
import io.wispforest.owo.braid.widgets.flex.Column;
import io.wispforest.owo.braid.widgets.label.Label;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.InputSupplier;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;

public class PngTexture extends StatefulWidget {
    private final InputSupplier<InputStream> input;

    public PngTexture(InputSupplier<InputStream> input) {
        this.input = input;
    }

    @Override
    public WidgetState<?> createState() {
        return new State();
    }

    public static class State extends WidgetState<PngTexture> {
        private static int MAX_TEXTURE_NUMBER = 0;

        private final Identifier textureId = Gadget.id("file_texture" + (MAX_TEXTURE_NUMBER++));
        private NativeImageBackedTexture prevTexture;

        private void updateTexture() {
            if (this.prevTexture != null) {
                MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            }

            try {
                prevTexture = new NativeImageBackedTexture(NativeImage.read(widget().input.get()));
                MinecraftClient.getInstance().getTextureManager().registerTexture(textureId, prevTexture);
            } catch (IOException e) {
                Gadget.LOGGER.error("Couldn't read texture from input stream!", e);
                // TODO: show exception.
            }
        }

        @Override
        public void init() {
            updateTexture();
        }

        @Override
        public void didUpdateWidget(PngTexture oldWidget) {
            updateTexture();
        }

        @Override
        public Widget build(BuildContext context) {
            if (prevTexture == null) return new Padding(Insets.none());

            return new Column(
                new Sized(
                    prevTexture.getImage().getWidth(),
                    prevTexture.getImage().getHeight(),
                    new TextureWidget(
                        textureId,
                        0,
                        0,
                        prevTexture.getImage().getWidth(),
                        prevTexture.getImage().getHeight(),
                        prevTexture.getImage().getWidth(),
                        prevTexture.getImage().getHeight(),
                        true
                    )
                ),
                new Align(
                    Alignment.LEFT,
                    new Label(Text.translatable(
                        "text.gadget.image_size",
                        prevTexture.getImage().getWidth(),
                        prevTexture.getImage().getHeight(),
                        "PNG"
                    ))
                )
            );
        }

        @Override
        public void dispose() {
            if (this.prevTexture == null) return;

            MinecraftClient.getInstance().getTextureManager().destroyTexture(textureId);
            this.prevTexture = null;
        }
    }
}
