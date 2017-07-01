/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.bukkit.manager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import io.github.wysohn.triggerreactor.bridge.player.IPlayer;
import io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer;
import io.github.wysohn.triggerreactor.core.manager.event.IPlayerPermissionCheckEvent;

/**
 * This event is designed to intercept the permission. You might can create CustomTrigger that hook
 *  io.github.wysohn.triggerreactor.manager.event.PlayerPermissionCheckEvent, and by canceling this event,
 *  you can enforce the permissible action not from permission plugins but your CustomTrigger. You can check
 *  'requestedPermission' to see what permission it is checking, and use setAllowed() function to change the
 *  permission check up behavior.
 * @author wysohn
 *
 */
public class PlayerPermissionCheckEvent extends PlayerEvent implements IPlayerPermissionCheckEvent, Cancellable{
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled;

    private final String requestedPermission;
    private boolean allowed = false;

	@Override
    public boolean isCancelled() {
		return cancelled;
	}

	@Override
    public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	@Override
    public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public PlayerPermissionCheckEvent(Player player, String requestedPermission) {
        super(player);
        this.requestedPermission = requestedPermission;
    }

    @Override
    public IPlayer getIPlayer() {
        return new BukkitPlayer(getPlayer());
    }

    @Override
    public String getRequestedPermission() {
        return requestedPermission;
    }

    @Override
    public boolean isAllowed() {
        return allowed;
    }

    @Override
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }


}
