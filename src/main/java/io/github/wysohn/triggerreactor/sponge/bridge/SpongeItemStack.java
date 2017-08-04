package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.item.inventory.ItemStack;

import io.github.wysohn.triggerreactor.core.bridge.IItemStack;

public class SpongeItemStack implements IItemStack {
    private final ItemStack itemStack;

    public SpongeItemStack(ItemStack itemStack) {
        super();
        this.itemStack = itemStack;
    }

    @Override
    public <T> T get() {
        return (T) itemStack;
    }

    /**
     * Returns the full name of item as String.
     */
    @Override
    public <T> T getType() {
        return (T) itemStack.getItem().getType().getId();
    }

    @Override
    public IItemStack clone() {
        return new SpongeItemStack(itemStack.copy());
    }

}
