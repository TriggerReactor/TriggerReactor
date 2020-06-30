package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitInventory;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorCore;
import io.github.wysohn.triggerreactor.core.manager.AbstractInventoryEditManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class InventoryEditManager extends AbstractInventoryEditManager implements Listener {

    private static final String message = "tellraw @p [\"\",{\"text\":\"" + CHECK + " Save\",\"bold\":true,\"underlined\":false,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditsave\"}},{\"text\":\"\\n\"},{\"text\":\"" + PENCIL + " Continue Editing\",\"bold\":true,\"underlined\":false,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditcontinue\"}},{\"text\":\"\\n\"},{\"text\":\"" + X + " Cancel\",\"bold\":true,\"underlined\":false,\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditdiscard\"}}]";

    public InventoryEditManager(TriggerReactorCore plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
    }

    @Override
    public void saveAll() {
    }

    @Override
    public void startEdit(IPlayer player, InventoryTrigger trigger) {
        UUID u = player.getUniqueId();
        if (sessions.containsKey(u)) {
            return;
        }
        sessions.put(u, trigger);

        IItemStack[] iitems = trigger.getItems();
        ItemStack[] items = new ItemStack[iitems.length];
        for (int i = 0; i < items.length; i++) {
            items[i] = iitems[i].get();
        }

        Inventory inv = Bukkit.createInventory(null, items.length, trigger.getTriggerName());
        inv.setContents(items);
        player.openInventory(new BukkitInventory(inv));
    }

    @Override
    public void continueEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!suspended.containsKey(u)) {
            return;
        }
        IInventory inv = suspended.remove(u);
        player.openInventory(inv);
    }

    @Override
    public void discardEdit(IPlayer player) {
        if (!sessions.containsKey(player.getUniqueId())) {
            return;
        }
        stopEdit(player);
        player.sendMessage("Discarded edits");
    }

    @Override
    public void saveEdit(IPlayer player) {
        UUID u = player.getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        Inventory inv = suspended.get(u).get();
        InventoryTrigger trigger = sessions.get(u);

        ItemStack[] items = inv.getStorageContents();
        IItemStack[] iitems = new IItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            iitems[i] = new BukkitItemStack(items[i] == null ? new ItemStack(Material.AIR) : items[i]);
        }

        replaceItems(trigger, iitems);
        stopEdit(player);
        player.sendMessage("Saved edits");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent e) {
        UUID u = e.getPlayer().getUniqueId();
        if (!sessions.containsKey(u)) {
            return;
        }
        //filter out already suspended
        if (suspended.containsKey(u)) {
            return;
        }
        Inventory inv = e.getInventory();

        suspended.put(u, new BukkitInventory(inv));
        sendMessage((Player) e.getPlayer());
    }

    private void sendMessage(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message.replace("@p", player.getName()));
    }
}
