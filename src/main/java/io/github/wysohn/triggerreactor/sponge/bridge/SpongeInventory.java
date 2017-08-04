package io.github.wysohn.triggerreactor.sponge.bridge;

import org.spongepowered.api.item.inventory.Inventory;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;

public class SpongeInventory implements IInventory {
    private final Inventory inventory;

    public SpongeInventory(Inventory inventory) {
        super();
        this.inventory = inventory;
    }



    @Override
    public <T> T get() {
        return (T) inventory;
    }

}
