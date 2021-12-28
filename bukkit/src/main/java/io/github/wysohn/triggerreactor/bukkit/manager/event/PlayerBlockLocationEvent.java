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

import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * This event fires depends on the player's block location. Unlike the PlayerMoveEvent, it only checks wether a
 * player moved
 * from a block to block. This significantly reduces the server load when you want to check player entering area, etc.
 *
 * @author wysohn
 */
public class PlayerBlockLocationEvent extends PlayerEvent implements Cancellable {
    private final SimpleLocation from;
    private final SimpleLocation to;
    private boolean cancelled;

    public PlayerBlockLocationEvent(Player who, SimpleLocation from, SimpleLocation to) {
        super(who);
        this.from = from;
        this.to = to;
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

    public SimpleLocation getFrom() {
        return from;
    }

    public SimpleLocation getTo() {
        return to;
    }

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
