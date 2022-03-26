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

var manager = api.getAreaManager();
var Entity = Java.type('org.bukkit.entity.Entity');
validation={
    "overloads" : [
        [{}],
        [{"type": "bool"}],
        [{"type": Entity}],
        [{"type": Entity}, {"type": "bool"}]
    ]
}

function currentarea(args) {
    switch(overload){
        case 0:
            if(player === null)
                return [];

            return manager.getAreas(LocationUtil.convertToSimpleLocation(location)).stream()
                .map(Map.Entry::getValue)
                .map(Trigger::getInfo)
                .map(TriggerInfo::getTriggerName)
                .toArray(String[]::new);
        case 1:
        case 2:
            var areas = manager.getAreas(LocationUtil.convertToSimpleLocation(location)).stream()
                            .map(Map.Entry::getValue)
                            .map(Trigger::getInfo)
                            .map(TriggerInfo::getTriggerName)
                            .toArray(String[]::new);
        case 1:
            return areas;
        case 2:
            if (args[1])
                return areas.length === 0 ? [] : areas[0]
            else
                return areas;
        default:
            return null;
    }
}