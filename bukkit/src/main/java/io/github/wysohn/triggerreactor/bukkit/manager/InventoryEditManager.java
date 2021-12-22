package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.core.bridge.IInventory;
import io.github.wysohn.triggerreactor.core.bridge.IItemStack;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.main.TriggerReactorMain;
import io.github.wysohn.triggerreactor.core.manager.AbstractInventoryEditManager;
import io.github.wysohn.triggerreactor.core.manager.trigger.inventory.InventoryTrigger;
import io.github.wysohn.triggerreactor.core.script.wrapper.IScriptObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class InventoryEditManager extends AbstractInventoryEditManager implements Listener {
    @Inject
    IWrapper wrapper;

    private static final String message = "tellraw @p [\"\",{\"text\":\"" + CHECK + " Save\",\"bold\":true,\"underlined\":false,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditsave\"}},{\"text\":\"\\n\"},{\"text\":\"" + PENCIL + " Continue Editing\",\"bold\":true,\"underlined\":false,\"color\":\"yellow\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditcontinue\"}},{\"text\":\"\\n\"},{\"text\":\"" + X + " Cancel\",\"bold\":true,\"underlined\":false,\"color\":\"red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/trg links inveditdiscard\"}}]";

    @Inject
    InventoryEditManager(TriggerReactorMain plugin) {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() {
    }

    @Override
    public void onDisable() {

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
            items[i] = Optional.ofNullable(iitems[i])
                    .map(IScriptObject::get)
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .orElse(null);
        }

        Inventory inv = Bukkit.createInventory(null, items.length, trigger.getInfo().getTriggerName());
        inv.setContents(items);
        player.openInventory(wrapper.wrap(inv));
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

        suspended.put(u, wrapper.wrap(inv));
        sendMessage((Player) e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        stopEdit(wrapper.wrap(event.getPlayer()));
    }

    private void sendMessage(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), message.replace("@p", player.getName()));
    }
}
