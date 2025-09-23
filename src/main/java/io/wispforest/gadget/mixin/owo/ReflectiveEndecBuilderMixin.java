package io.wispforest.gadget.mixin.owo;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.ReflectiveEndecBuilder;
import io.wispforest.gadget.desc.edit.PrimitiveEditType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

// don't you love it when the library breaks your stuff
// TODO: remove this when owo-lib fixes itself
@Mixin(value = ReflectiveEndecBuilder.class, remap = false)
public abstract class ReflectiveEndecBuilderMixin {
    @Shadow @Nullable protected abstract <T> Endec<T> getOrNull(Class<T> clazz);

    @Inject(method = "getAnnotated(Ljava/lang/reflect/AnnotatedType;Ljava/lang/reflect/Type;)Lio/wispforest/endec/Endec;", at = @At("HEAD"), cancellable = true)
    private void fixBug(AnnotatedType annotatedType, @Nullable Type baseType, CallbackInfoReturnable<Endec<?>> cir) {
        if (annotatedType.getType().getTypeName().contains("io.wispforest.gadget.desc.edit.PrimitiveEditType")) {
            cir.setReturnValue(getOrNull(PrimitiveEditType.class));
        }
    }
}
