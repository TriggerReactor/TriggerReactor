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
package io.github.wysohn.triggerreactor.bukkit.manager.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event is designed to intercept the permission. You might can create CustomTrigger that hook
 * io.github.wysohn.triggerreactor.bukkit.manager.event.PlayerPermissionCheckEvent, and by canceling this event,
 * you can enforce the permissible action not from permission plugins but your CustomTrigger. You can check
 * 'requestedPermission' to see what permission it is checking, and use setAllowed() function to change the
 * permission check up behavior.
 *
 * @author wysohn
 */
public class PlayerPermissionCheckEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final String requestedPermission;
    private boolean cancelled;
    private boolean allowed = false;

    public PlayerPermissionCheckEvent(Player player, String requestedPermission) {
        super(player);
        this.requestedPermission = requestedPermission;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public String getRequestedPermission() {
        return requestedPermission;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }


}
