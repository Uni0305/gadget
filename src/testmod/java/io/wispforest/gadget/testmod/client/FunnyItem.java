package io.wispforest.gadget.testmod.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FunnyItem extends Item {
    public FunnyItem() {
        super(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("gadget-testmod", "funny"))));
    }

    @Override
    public Text getName(ItemStack stack) {
        if (Screen.hasShiftDown()) {
            // todo: fix this.
//            stack.getOrCreateNbt().putString("owl", "yay");
        }

        return super.getName(stack);
    }
}
