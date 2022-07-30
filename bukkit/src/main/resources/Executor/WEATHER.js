/*******************************************************************************
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
var World = Java.type('org.bukkit.World');

var validation = {
  overloads: [
    [{ name: 'isStorm', type: 'boolean' }],
    [
      { name: 'world', type: 'string' },
      { name: 'isStorm', type: 'boolean' }
    ],
    [
      { name: 'world', type: World.class },
      { name: 'isStorm', type: 'boolean' }
    ]
  ]
};

function WEATHER(args) {
  var world, isStorm;

  if (overload === 0) {
    world = player.getLocation().getWorld();
    isStorm = args[0];
  } else if (overload === 1) {
    world = Bukkit.getWorld(args[0]);
    isStorm = args[1];
  } else if (overload === 2) {
    world = args[0];
    isStorm = args[1];
  }

  if (!world) throw new Error('Unknown world named ' + args[0]);

  world.setStorm(isStorm);

  return null;
}
