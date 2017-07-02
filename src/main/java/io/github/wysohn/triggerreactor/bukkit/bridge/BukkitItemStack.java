package io.github.wysohn.triggerreactor.bukkit.bridge;

import org.bukkit.inventory.ItemStack;

import io.github.wysohn.triggerreactor.bridge.IItemStack;

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

    @Override
    public int getTypeId() {
        return itemStack.getTypeId();
    }

    @Override
    public IItemStack clone() {
        return new BukkitItemStack(itemStack.clone());
    }

}
