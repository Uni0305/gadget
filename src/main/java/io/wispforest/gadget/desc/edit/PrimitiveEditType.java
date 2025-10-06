package io.wispforest.gadget.desc.edit;

import io.wispforest.endec.Endec;
import io.wispforest.endec.annotations.SealedPolymorphic;

// This makes ReflectiveEndecBuilder not want to try to wrap this into an ObjectEndec, which would fail.
@SealedPolymorphic
public interface PrimitiveEditType<T> {
    Endec<PrimitiveEditType<?>> ENDEC = PrimitiveEditTypes.ENDEC;

    T fromPacket(String repr);
    String toPacket(T value);
}