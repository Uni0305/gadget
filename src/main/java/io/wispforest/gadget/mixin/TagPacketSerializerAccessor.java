package io.wispforest.gadget.mixin;

import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagPacketSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TagPacketSerializer.class)
public interface TagPacketSerializerAccessor {
    @Invoker("serializeTags")
    static <T> TagPacketSerializer.Serialized serializeTags(Registry<T> registry) {
        throw new IllegalStateException();
    }
}
