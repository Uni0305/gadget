package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

public class TabTextBoxComponent extends TextBoxComponent {
    public TabTextBoxComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_TAB) {
            // Pass the event to the root component.
            root().onKeyPress(input);

            return true;
        }

        return super.keyPressed(input);
    }
}
