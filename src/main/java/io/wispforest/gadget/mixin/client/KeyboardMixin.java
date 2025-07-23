package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.wispforest.gadget.Gadget;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract boolean processF3(int key);

    // TODO: look at whether this mixin still works.
    @ModifyExpressionValue(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"))
    private boolean afterKeyPressed(boolean original, long window, int key, int scancode, int action, int modifiers) {
        var client = MinecraftClient.getInstance();

        if (original) return true;
        if (!Gadget.CONFIG.debugKeysInScreens()) return false;
        if (!InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_F3)) return false;

        return ((KeyboardMixin)(Object) client.keyboard).processF3(key);
    }

    @Inject(method = "processF3", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;"), cancellable = true)
    private void leaveIfPlayer(int key, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null)
            cir.setReturnValue(false);
    }

    @Inject(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z"), cancellable = true)
    private void leaveOnGameModeSelection(int key, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null)
            cir.setReturnValue(false);
    }
}
