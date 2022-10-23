/*******************************************************************************
 *     Copyright (C) 2018 wysohn
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package io.github.wysohn.triggerreactor.bukkit.manager;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitBlock;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.AreaSelectionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class AreaSelectionListener implements Listener {
    private AreaSelectionManager manager;

    public AreaSelectionListener(AreaSelectionManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.resetSelections(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        AreaSelectionManager.ClickAction action = null;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            action = AreaSelectionManager.ClickAction.LEFT_CLICK_BLOCK;
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            action = AreaSelectionManager.ClickAction.RIGHT_CLICK_BLOCK;
        }

        manager.onInteract(new BukkitPlayer(player),
                   BukkitUtil.isLeftHandClick(e),
                   e::setCancelled,
                   new BukkitBlock(e.getClickedBlock()),
                   action);
    }
}