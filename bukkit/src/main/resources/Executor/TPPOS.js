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

var Entity = Java.type('org.bukkit.entity.Entity');
var Location = Java.type('org.bukkit.Location');

var validation = {
  overloads: [
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { type: 'string', name: 'x' },
      { type: 'string', name: 'y' },
      { type: 'string', name: 'z' },
    ],
    [
      { 'type': Entity.class, name: 'entity' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
    ],
    [
      { 'type': Entity.class, name: 'entity' },
      { type: 'string', name: 'x' },
      { type: 'string', name: 'y' },
      { type: 'string', name: 'z' },
    ]
  ]
};

function TPPOS(args) {
  var target, location

  if (overload === 0) {
    target = player;
    location = new Location(
      target.getLocation().getWorld(),
      args[0],
      args[1],
      args[2]
    );
  } else if (overload === 1) {
    target = player;
    location = new Location(
      target.getLocation().getWorld(),
      args[0].indexOf('~') === 0 ? target.getLocation().getX() + parseInt(args[0].split('~')[1]) : parseInt(args[0]),
      args[1].indexOf('~') === 0 ? target.getLocation().getY() + parseInt(args[1].split('~')[1]) : parseInt(args[1]),
      args[2].indexOf('~') === 0 ? target.getLocation().getZ() + parseInt(args[2].split('~')[1]) : parseInt(args[2])
    );
  } else if (overload === 2) {
    target = args[0];
    location = new Location(
      target.getLocation().getWorld(),
      args[1],
      args[2],
      args[3]
    )
  } else if (overload === 3) {
    target = args[0];
    location = new Location(
      target.getLocation().getWorld(),
      args[1].indexOf('~') === 0 ? target.getLocation().getX() + parseInt(args[1].split('~')[1]) : parseInt(args[1]),
      args[2].indexOf('~') === 0 ? target.getLocation().getY() + parseInt(args[2].split('~')[1]) : parseInt(args[2]),
      args[3].indexOf('~') === 0 ? target.getLocation().getZ() + parseInt(args[3].split('~')[1]) : parseInt(args[3])
    );
  }

  if (!target) throw new Error('Player is null.');

  target.teleport(location);

  return null;
}