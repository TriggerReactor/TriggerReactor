package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.InventoryEditManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import javax.inject.Inject;
import java.util.UUID;

public final class InventoryEditListener extends AbstractBukkitListener {
    @Inject
    InventoryEditManager inventoryEditManager;
    @Inject
    IWrapper wrapper;

    @Inject
    InventoryEditListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (!inventoryEditManager.hasSession(u)) {
            return;
        }
        //filter out already suspended
        if (inventoryEditManager.hasSuspended(u)) {
            return;
        }
        Inventory inv = e.getInventory();

        inventoryEditManager.putSuspend(u, wrapper.wrap(inv));
        sendMessage((Player) e.getPlayer());
    }

    private void sendMessage(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message.replace("@p", player.getName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inventoryEditManager.stopEdit(wrapper.wrap(event.getPlayer()));
    }

    protected static final char X = '\u2718';
    protected static final char CHECK = '\u2713';
    protected static final char PENCIL = '\u270E';

    private static final String message =
            "tellraw @p [\"\",{\"text\":\"" + CHECK + " Save\",\"bold\":true,\"underlined\":false,\"color\":\"green\","
                    + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditsave\"}},"
                    + "{\"text\":\"\\n\"},"
                    + "{\"text\":\"" + PENCIL
                    + " Continue Editing\",\"bold\":true,\"underlined\":false,\"color\":\"yellow\","
                    + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditcontinue\"}},"
                    + "{\"text\":\"\\n\"},{\"text\":\"" + X
                    + " Cancel\",\"bold\":true,\"underlined\":false,\"color\":\"red\","
                    + "\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditdiscard\"}}]";

}
