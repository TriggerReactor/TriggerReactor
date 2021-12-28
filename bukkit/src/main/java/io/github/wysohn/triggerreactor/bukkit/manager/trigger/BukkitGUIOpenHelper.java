package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.IGUIOpenHelper;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTriggerManager;
import io.github.wysohn.triggerreactor.tools.ValidationUtil;
import org.bukkit.entity.Player;

import javax.inject.Inject;

public class BukkitGUIOpenHelper implements IGUIOpenHelper {
    @Inject
    IWrapper wrapper;
    @Inject
    InventoryTriggerManager inventoryTriggerManager;

    @Inject
    BukkitGUIOpenHelper() {

    }

    /**
     * @param player
     * @param name
     * @return the opened Inventory's reference; null if no Inventory Trigger found
     */
    public IInventory openGUI(Object player, String name) {
        ValidationUtil.assertTrue(player, v -> v instanceof Player);
        IPlayer bukkitPlayer = wrapper.wrap(player);
        return inventoryTriggerManager.openGUI(bukkitPlayer, name);
    }
}
