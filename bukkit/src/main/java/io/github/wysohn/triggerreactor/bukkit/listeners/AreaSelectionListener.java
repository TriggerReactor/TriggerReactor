package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.AreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.selection.ClickResult;
import io.github.wysohn.triggerreactor.core.manager.selection.ClickType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public final class AreaSelectionListener extends AbstractBukkitListener {
    @Inject
    AreaSelectionManager areaSelectionManager;

    @Inject
    AreaSelectionListener() {

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!areaSelectionManager.isSelecting(uuid))
            return;

        e.setCancelled(true);

        if (!BukkitUtil.isLeftHandClick(e))
            return;

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getClickedBlock().getLocation());

        ClickResult result = null;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            result = areaSelectionManager.onClick(ClickType.LEFT_CLICK, uuid, sloc);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            result = areaSelectionManager.onClick(ClickType.RIGHT_CLICK, uuid, sloc);
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
