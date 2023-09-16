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
validation = {
    "overloads": [
        [],
    ]
}

var Keys = Java.type('org.spongepowered.api.data.key.Keys');
var AffectEntityEvent = Java.type('org.spongepowered.api.event.entity.AffectEntityEvent');
var TargetEntityEvent = Java.type('org.spongepowered.api.event.entity.TargetEntityEvent');

function entityname(args) {
    var entityName = null;

    if (event instanceof AffectEntityEvent) {
        var entities = event.getEntities();
        if(entities.size() < 1)
            return null;

        entityName = entities.get(0).get(Keys.DISPLAY_NAME).orElse(null);
    } else if (event instanceof TargetEntityEvent) {
        entity = event.getTargetEntity();
        if(entity == null)
            return null;

        entityName = entity.get(Keys.DISPLAY_NAME).orElse(null);
    } else {
        throw new Error('$slot Placeholder is available only in CustomTrigger!')
    }

    return entityName;
}