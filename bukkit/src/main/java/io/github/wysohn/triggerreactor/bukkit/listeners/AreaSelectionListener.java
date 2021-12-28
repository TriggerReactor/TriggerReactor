package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.util.UUID;

public final class AreaSelectionListener extends AbstractBukkitListener {
    @Inject
    AreaSelectionManager areaSelectionManager;

    @Inject
    AreaSelectionListener(){

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!areaSelectionManager.hasSelection(uuid)) return;

        e.setCancelled(true);

        if (!BukkitUtil.isLeftHandClick(e)) return;

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getClickedBlock()
                .getLocation());

        AreaSelectionManager.ClickResult result = null;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            result = areaSelectionManager.onClick(AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK, uuid, sloc);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            result = areaSelectionManager.onClick(AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK, uuid, sloc);
        }

        if (result != null) {
            switch (result) {
                case DIFFERENTWORLD:
                    player.sendMessage(ChatColor.RED + "Positions have different world name.");
                    break;
                case COMPLETE:
                    SimpleLocation left = areaSelectionManager.getLeftPosition(uuid);
                    SimpleLocation right = areaSelectionManager.getRightPosition(uuid);

                    SimpleLocation smallest = SimpleLocation.getSmallest(left, right);
                    SimpleLocation largest = SimpleLocation.getLargest(left, right);

                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Smallest: " + smallest + " , Largest: " + largest);
                    break;
                case LEFTSET:
                    player.sendMessage(ChatColor.GREEN + "Left ready");
                    break;
                case RIGHTSET:
                    player.sendMessage(ChatColor.GREEN + "Right ready");
                    break;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        areaSelectionManager.resetSelections(e.getPlayer().getUniqueId());
    }
}
