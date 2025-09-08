package io.wispforest.gadget.client;

import io.wispforest.owo.mixin.ScreenAccessor;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;

public class GadgetSurfaces {
    public static final Surface OPTIONS_BACKGROUND = Surface
            .panorama(ScreenAccessor.owo$ROTATING_PANORAMA_RENDERER(), false)
            .and((context, component) -> MinecraftClient.getInstance().gameRenderer.renderBlur())
            .and(Surface.VANILLA_TRANSLUCENT);
}
