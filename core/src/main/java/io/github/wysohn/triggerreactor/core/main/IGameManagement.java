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

package io.github.wysohn.triggerreactor.core.main;

import io.github.wysohn.triggerreactor.core.bridge.ICommandSender;
import io.github.wysohn.triggerreactor.core.bridge.IWorld;
import io.github.wysohn.triggerreactor.core.bridge.entity.IPlayer;
import io.github.wysohn.triggerreactor.core.manager.location.SimpleLocation;
import io.github.wysohn.triggerreactor.core.manager.trigger.Trigger;

import java.util.Map;
import java.util.Set;

/**
 * This interface provides methods to manage various game related things.
 * <p>
 * For example, this interface can be used to spawn an entity, change the time of
 * the day, etc.
 * <p>
 * But this is not the place to manage other things such as permissions, commands, etc.
 * that are not directly related to the natural behavior of the game. If you are confused,
 * as yourself "Is this exist even without the plugin?" If the answer is no, then this
 * is not the place to put it.
 */
public interface IGameManagement {
    Iterable<IPlayer> getOnlinePlayers();

    Iterable<IWorld> getWorlds();

    IWorld getWorld(String world);

    IPlayer getPlayer(String playerName);

    /**
     * Show glowstones to indicate the walk/click triggers in the chunk. This should send block change packet
     * instead of changing the real block.
     *
     * @param sender sender to show the glow stones
     * @param set    the set contains location of block and its associated trigger.
     */
    void showGlowStones(ICommandSender sender, Set<Map.Entry<SimpleLocation, Trigger>> set);
}
