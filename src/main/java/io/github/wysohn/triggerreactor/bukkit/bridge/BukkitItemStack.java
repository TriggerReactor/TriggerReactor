package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.inventory.ItemStack;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;

public class BukkitItemStack implements IItemStack {
    private final ItemStack itemStack;

    public BukkitItemStack(ItemStack itemStack) {
        super();
        this.itemStack = itemStack;
    }

    @Override
    public <T> T get() {
        return (T) itemStack;
    }

    /**
     * Returns the Material of the item
     */
    @Override
    public <T> T getType() {
        return (T) itemStack.getType();
    }

    @Override
    public IItemStack clone() {
        return new BukkitItemStack(itemStack.clone());
    }

}
