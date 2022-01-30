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
package io.github.wysohn.triggerreactor.core.manager;

import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.bridge.event.IPlayerBlockLocationEvent;
import io.github.wysohn.triggerreactor.core.main.IGameController;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.scope.ManagerScope;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ManagerScope
public class PlayerLocationManager extends Manager {
    private final transient Map<UUID, SimpleLocation> locations = new ConcurrentHashMap<>();

    @Inject
    IGameController gameController;

    @Inject
    PlayerLocationManager() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() throws Exception {

    }

    @Override
    public void onReload() throws RuntimeException {
        for (IPlayer player : gameController.getOnlinePlayers()) {
            SimpleLocation sloc = player.getLocation();
            setCurrentBlockLocation(player.getUniqueId(), sloc);
        }
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
     * <b>The child class should call this method manually when a player moved from a block to another block.</b>
     */
    public void checkIfCancelled(IPlayerBlockLocationEvent event) {
        if (event.getFrom().equals(event.getTo()))
            return;

        gameController.callEvent(event);
        if (!event.isCancelled()) {
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