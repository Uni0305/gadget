package io.wispforest.gadget.mixin.owo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "io.wispforest.owo.network.OwoNetChannel$MessagePayload")
public interface MessagePayloadAccessor {
    @Accessor(remap = false)
    Record getMessage();
}
