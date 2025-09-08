package io.wispforest.gadget.mixin.owo;

import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(targets = "io.wispforest.owo.network.OwoHandshake$HandshakeResponse")
public interface HandshakeResponseAccessor {
    @Accessor(remap = false)
    Map<Identifier, Integer> getRequiredChannels();

    @Accessor(remap = false)
    Map<Identifier, Integer> getRequiredControllers();

    @Accessor(remap = false)
    Map<Identifier, Integer> getOptionalChannels();
}
