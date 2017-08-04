package io.github.wysohn.triggerreactor.core.bridge;

/**
 * For the sake of InventoryTrigger support, all child classes must override hashCode() and equals()
 * method out of the actual Inventory class.
 * @author wysohn
 *
 */
public interface IInventory extends IMinecraftObject{

    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);

}
