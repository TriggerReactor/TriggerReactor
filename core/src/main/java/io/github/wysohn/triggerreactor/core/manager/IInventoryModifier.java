package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;

public interface IInventoryModifier {
    IInventory createInventory(String name, IItemStack[] contents);

    IInventory createInventory(int size, String name);

    boolean removeLore(IItemStack iS, int index);

    void setItemTitle(IItemStack iS, String title);

    boolean setLore(IItemStack iS, int index, String lore);
}
