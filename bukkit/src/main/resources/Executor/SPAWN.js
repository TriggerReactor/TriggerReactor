/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *     Copyright (C) 2022 Ioloolo
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

var EntityType = Java.type('org.bukkit.entity.EntityType');
var Player = Java.type('org.bukkit.entity.Player');
var Location = Java.type('org.bukkit.Location');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'entity' }
    ],
    [
      { type: EntityType.class, name: 'entity' }
    ],
    [
      { type: Location.class, name: 'location' },
      { type: 'string', name: 'entity' }
    ],
    [
      { type: Location.class, name: 'location' },
      { type: EntityType.class, name: 'entity' }
    ]
  ]
};

function SPAWN(args) {
  var location, entity;

  if (overload === 0) {
    location = player.getLocation();
    entity = EntityType.valueOf(args[0].toUpperCase());
  } else if (overload === 1) {
    location = player.getLocation();
    entity = args[0];
  } else if (overload === 2) {
    location = args[0];
    entity = EntityType.valueOf(args[1].toUpperCase());
  } else if (overload === 3) {
    location = args[0];
    entity = args[1];
  }

  if (!entity)
    throw new Error(
      overload >= 2 ? args[1] : args[0] + ' is not valid material.'
    );

  location.getWorld().spawnEntity(location, entity);

  return null;
}
