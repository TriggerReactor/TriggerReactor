package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;

public interface IInventoryHandle<ItemStack> {
    /**
     * Create actual inventory.
     *
     * @param size size of inventory. Must be multiple of 9.
     * @param name name of the inventory. This is the raw name, so the
     *             implemented method has to translate color code and and
     *             underscore appropriately.
     * @return the inventory
     */
    IInventory createInventory(int size, String name);

    /**
     * @param trigger
     * @param size      mutiple of 9; must be less than or equalt to 54 (exclusive)
     * @param inventory
     */
    void fillInventory(InventoryTrigger trigger, int size, IInventory inventory);

    Class<ItemStack> getItemClass();

    IItemStack wrapItemStack(ItemStack item);
}
