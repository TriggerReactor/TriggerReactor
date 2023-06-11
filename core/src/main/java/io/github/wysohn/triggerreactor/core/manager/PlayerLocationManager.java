/*
 * Copyright (C) 2022. TriggerReactor Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.ILocation;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.main.IEventManagement;
import io.github.wysohn.triggerreactor.core.main.IGameManagement;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public final class PlayerLocationManager extends Manager {
    @Inject
    private IGameManagement gameManagement;
    @Inject
    private IEventManagement eventManagement;

    private final Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    @Inject
    private PlayerLocationManager() {
        super();
    }

    @Override
    public void initialize() {

    }

    @Override
    public void reload() {
        for (IPlayer player : gameManagement.getOnlinePlayers()) {
            ILocation loc = player.getLocation();
            setCurrentBlockLocation(player.getUniqueId(), loc.toSimpleLocation());
        }
    }

    @Override
    public void shutdown() {

    }

    /**
     * set current location of the player
     *
     * @param uuid the player's uuid
     * @param sloc the location where player is at
     */
    public void setCurrentBlockLocation(UUID uuid, SimpleLocation sloc) {
        locations.put(uuid, sloc);
    }

    /**
     * get location of player
     *
     * @param uuid uuid of player
     * @return the location. If the player just logged in, it might be null.
     */
    public SimpleLocation getCurrentBlockLocation(UUID uuid) {
        return locations.get(uuid);
    }

    /**
     * Called when a player moved from one block to another.
     *
     * @param event the event fired when a player moved.
     * @deprecated event handler. Should be called by listener or tests only.
     */
    public void onMove(IPlayerBlockLocationEvent event) {
        if (event.getFrom().equals(event.getTo()))
            return;

        eventManagement.callEvent(event);
        if (event.isCancelled()) {
            event.setCancelled(true);
        } else {
            setCurrentBlockLocation(event.getIPlayer().getUniqueId(), event.getTo());
        }
    }

    /**
     * remove the current location of the player.
     *
     * @param uuid the player's uuid
     */
    public void removeCurrentBlockLocation(UUID uuid) {
        locations.remove(uuid);
    }
}