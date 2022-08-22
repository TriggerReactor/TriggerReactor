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
package io.github.wysohn.triggerreactor.bukkit.manager.trigger;

import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitBlock;
import io.github.wysohn.triggerreactor.bukkit.bridge.BukkitItemStack;
import io.github.wysohn.triggerreactor.bukkit.bridge.entity.BukkitPlayer;
import io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil;
import io.github.wysohn.triggerreactor.core.manager.trigger.location.LocationBasedTriggerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClickTriggerListener
        extends LocationBasedTriggerListener<LocationBasedTriggerManager.ClickTrigger,
        LocationBasedTriggerManager<LocationBasedTriggerManager.ClickTrigger>> {


    public ClickTriggerListener(LocationBasedTriggerManager<LocationBasedTriggerManager.ClickTrigger> manager) {
        super(manager);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickTrigger(PlayerInteractEvent e) {
        if (!BukkitUtil.isLeftHandClick(e))
            return;

        manager.handleClick(e,
                            new BukkitBlock(e.getClickedBlock()),
                            new BukkitPlayer(e.getPlayer()),
                            new BukkitItemStack(e.getItem()),
                            toActivity(e.getAction()));
    }

}
