package io.wispforest.gadget.desc.edit;

import io.wispforest.endec.Endec;
import io.wispforest.endec.annotations.SealedPolymorphic;

// SealedPolymorphic is here to trick the ReflectiveEndecBuilder to use our registered type...
// Otherwise, it just seems to ignore it, I don't know...
@SealedPolymorphic
public interface PrimitiveEditType<T> {
    Endec<PrimitiveEditType<?>> ENDEC = PrimitiveEditTypes.ENDEC;

    T fromPacket(String repr);
    String toPacket(T value);
}
