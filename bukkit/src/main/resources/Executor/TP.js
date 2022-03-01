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
var Bukkit = Java.type('org.bukkit.Bukkit')
var Location = Java.type('org.bukkit.Location')
var Player = Java.type('org.bukkit.entity.Player')

var Executor = Java.type('io.github.wysohn.triggerreactor.core.script.interpreter.Executor')

validation = {
  overloads: [
    [{ name: 'location', type: Location.class }],
    [{ name: 'player', type: Player.class }],
    [
      // Overloads index: 2
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' }
    ],
    [
      // Overloads index: 3
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'yaw', type: 'number' },
      { name: 'pitch', type: 'number' }
    ],
    [
      // Overloads index: 4
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'target', type: Player.class }
    ],
    [
      // Overloads index: 5
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'target', type: 'string' }
    ],
    [
      // Overloads index: 6
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'yaw', type: 'number' },
      { name: 'pitch', type: 'number' },
      { name: 'target', type: Player.class }
    ],
    [
      // Overloads index: 7
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'yaw', type: 'number' },
      { name: 'pitch', type: 'number' },
      { name: 'target', type: 'string' }
    ]
  ]
}

function TP(args) {
  var target = player,
    world = player.getWorld(),
    location;

  if (overload === 0) {
    location = args[0]
  } else if (overload === 1) {
    location = args[0].getLocation()
  } else if (overload === 2) {
    location = new Location(world, args[0], args[1], args[2])
  } else if (overload === 3) {
    location = new Location(world, args[0], args[1], args[2], args[3], args[4])
  } else if (overload === 4) {
    target = args[3]
    location = new Location(world, args[0], args[1], args[2])
  } else if (overload === 5) {
    target = Bukkit.getPlayer(args[3])
    if (target == null)
      throw new Error('Player not found with ' + args[3])

    location = new Location(world, args[0], args[1], args[2])
  } else if (overload === 6) {
    target = args[5]
    location = new Location(world, args[0], args[1], args[2], args[3], args[4])
  } else if (overload === 7) {
    target = Bukkit.getPlayer(args[5])
    if (target == null)
      throw new Error('Player not found with ' + args[5])

    location = new Location(world, args[0], args[1], args[2], args[3], args[4])
  }

  if (target == null)
    throw new Error('Player not found')

  target.teleport(location)
  return null;
}
