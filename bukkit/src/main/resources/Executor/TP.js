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

var Bukkit = Java.type('org.bukkit.Bukkit');
var Location = Java.type('org.bukkit.Location');
var Player = Java.type('org.bukkit.entity.Player');

var validation = {
  overloads: [
    [
      { type: Location.class , name: 'location'}
    ],
    [
      { type: Player.class, name: 'player'}
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' }
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'number', name: 'yaw' },
      { type: 'number', name: 'pitch' }
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: Player.class, name: 'target' }
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { name: 'target', type: 'string' }
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'number', name: 'yaw' },
      { type: 'number', name: 'pitch' },
      { type: Player.class, name: 'target' }
    ],
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' },
      { type: 'number', name: 'yaw' },
      { type: 'number', name: 'pitch' },
      { type: 'string', name: 'target' }
    ]
  ]
};

function TP(args) {
  var target, location;

  if (overload === 0) {
    target = player;
    location = args[0];
  } else if (overload === 1) {
    target = player;
    location = args[0].getLocation();
  } else if (overload === 2) {
    target = player;
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2]
    );
  } else if (overload === 3) {
    target = player;
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld('world'),
      args[0],
      args[1],
      args[2],
      args[3],
      args[4]
    );
  } else if (overload === 4) {
    target = args[3];
    location = new Location(
      target.getLocation().getWorld(),
      args[0],
      args[1],
      args[2]
    );
  } else if (overload === 5) {
    target = Bukkit.getPlayer(args[3]);
    location = new Location(
      target.getLocation().getWorld(),
      args[0],
      args[1],
      args[2]
    );
  } else if (overload === 6) {
    target = args[5];
    location = new Location(
      target.getLocation().getWorld(),
      args[0],
      args[1],
      args[2],
      args[3],
      args[4]
    );
  } else if (overload === 7) {
    target = Bukkit.getPlayer(args[5]);
    location = new Location(
      target.getLocation().getWorld(),
      args[0],
      args[1],
      args[2],
      args[3],
      args[4]
    );
  }

  if (!target) throw new Error('Player is null.');

  target.teleport(location);

  return null;
}
