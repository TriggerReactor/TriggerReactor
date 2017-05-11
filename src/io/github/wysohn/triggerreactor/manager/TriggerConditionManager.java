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
package io.github.wysohn.triggerreactor.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;

import io.github.wysohn.triggerreactor.core.interpreter.InterpretCondition;
import io.github.wysohn.triggerreactor.main.TriggerReactor;

public class TriggerConditionManager extends Manager implements InterpretCondition{

    private final Map<UUID, Map<String, Object>> conditions = new ConcurrentHashMap<>();
    public TriggerConditionManager(TriggerReactor plugin) {
        super(plugin);
    }

    @Override
    public void setCondition(Object context, String key, Object value){
        if(!(context instanceof PlayerEvent))
            return;

        Player player = ((PlayerEvent) context).getPlayer();

        Map<String, Object> prop = conditions.get(player.getUniqueId());
        if(prop == null){
            prop = new ConcurrentHashMap<>();
            conditions.put(player.getUniqueId(), prop);
        }

        prop.put(key, value);
    }

    @Override
    public Object getCondition(Object context, String key){
        if(!(context instanceof PlayerEvent))
            return null;

        Player player = ((PlayerEvent) context).getPlayer();

        if(!conditions.containsKey(player.getUniqueId()))
            return null;

        Map<String, Object> prop = conditions.get(player.getUniqueId());
        return prop.get(key);
    }

    @Override
    public void reload() {

    }

    @Override
    public void saveAll() {

    }

}
