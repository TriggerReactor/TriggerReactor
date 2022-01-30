package io.github.wysohn.triggerreactor.bukkit.listeners;

import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.main.IWrapper;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.selection.ClickType;
import io.github.wysohn.triggerreactor.core.manager.selection.LocationSelectionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocationSelectionListener extends AbstractBukkitListener {
    @Inject
    LocationSelectionManager locationSelectionManager;
    @Inject
    IWrapper wrapper;

    @Inject
    LocationSelectionListener() {

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        IPlayer player = wrapper.wrap(event.getPlayer());

        if (!locationSelectionManager.hasStarted(player))
            return;

        event.setCancelled(true);

        SimpleLocation clicked = LocationUtil.convertToSimpleLocation(event.getClickedBlock().getLocation());

        boolean handled = false;
        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK:
                handled = locationSelectionManager.onClick(clicked, player, ClickType.LEFT_CLICK);
                break;
            case RIGHT_CLICK_BLOCK:
                handled = locationSelectionManager.onClick(clicked, player, ClickType.RIGHT_CLICK);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + event.getAction());
        }

        event.setCancelled(!handled);
    }
}
