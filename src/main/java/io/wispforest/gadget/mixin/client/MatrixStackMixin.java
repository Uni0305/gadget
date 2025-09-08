package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.MatrixStackLogger;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MatrixStack.class)
public class MatrixStackMixin {
    @Shadow
    private int stackDepth;

    @Inject(method = "pop", at = @At("HEAD"), cancellable = true)
    private void onPop(CallbackInfo ci) {
        if (stackDepth == 0 && MatrixStackLogger.tripError(cast(), "Tried to pop empty MatrixStack")) {
            ci.cancel();
            return;
        }

        MatrixStackLogger.logOp((MatrixStack) (Object) this, false, stackDepth - 1);
    }

    @Unique
    private MatrixStack cast() {
        return (MatrixStack) (Object) this;
    }

    @Inject(method = "push", at = @At("HEAD"))
    private void onPush(CallbackInfo ci) {
        MatrixStackLogger.logOp((MatrixStack) (Object) this, true, stackDepth);
    }
}
