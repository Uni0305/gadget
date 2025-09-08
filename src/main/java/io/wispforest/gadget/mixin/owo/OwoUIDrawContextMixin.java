package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.PositionedRectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(OwoUIDrawContext.class)
public class OwoUIDrawContextMixin extends DrawContext {
    public OwoUIDrawContextMixin(MinecraftClient client, GuiRenderState state) {
        super(client, state);
    }

    /**
     * @author: DasBabyPixel
     * @reason Issue: <a href="https://github.com/wisp-forest/owo-lib/issues/428">GitHub</a>
     */
    @Overwrite(remap = false)
    public boolean intersectsScissor(PositionedRectangle other) {
        var scissor = this.scissorStack.peekLast();

        if (scissor == null) return true;

        var rect = new ScreenRect(new ScreenPos(other.x(), other.y()), other.width(), other.height()).transform(getMatrixStack());

        return scissor.intersects(rect);
    }
}
