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

import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.bukkit.tools.LocationUtil;
import io.github.wysohn.triggerreactor.core.manager.AbstractAreaSelectionManager;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class AreaSelectionManager extends AbstractAreaSelectionManager implements Listener {

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onReload() {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAll() {
        // TODO Auto-generated method stub

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!selecting.contains(uuid)) return;

        e.setCancelled(true);

        if (!BukkitUtil.isLeftHandClick(e)) return;

        SimpleLocation sloc = LocationUtil.convertToSimpleLocation(e.getClickedBlock().getLocation());

        ClickResult result = null;
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            result = onClick(ClickAction.LEFT_CLICK_BLOCK, uuid, sloc);
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            result = onClick(ClickAction.RIGHT_CLICK_BLOCK, uuid, sloc);
        }

        if (result != null) {
            switch (result) {
                case DIFFERENTWORLD:
                    player.sendMessage(ChatColor.RED + "Positions have different world name.");
                    break;
                case COMPLETE:
                    SimpleLocation left = leftPosition.get(uuid);
                    SimpleLocation right = rightPosition.get(uuid);

                    SimpleLocation smallest = getSmallest(left, right);
                    SimpleLocation largest = getLargest(left, right);

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
        resetSelections(e.getPlayer().getUniqueId());
    }
}