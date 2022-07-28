/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
var Sign = Java.type('org.bukkit.block.Sign');
var Location = Java.type('org.bukkit.Location');
var ChatColor = Java.type('org.bukkit.ChatColor');

var validation = {
  overloads: [
    [
      { name: 'line', type: 'int', minimum: 0, maximum: 3 },
      { name: 'text', type: 'string' },
      { name: 'location', type: Location.class }
    ],
    [
      { name: 'line', type: 'int', minimum: 0, maximum: 3 },
      { name: 'text', type: 'string' },
      { name: 'x', type: 'int' },
      { name: 'y', type: 'int' },
      { name: 'z', type: 'int' }
    ]
  ]
};

function SIGNEDIT(args) {
  var line, text, location;

  if (overload === 0) {
    line = args[0];
    text = args[1];
    location = args[2];
  } else if (overload === 1) {
    line = args[0];
    text = args[1];
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld('world'),
      args[2],
      args[3],
      args[4]
    );
  }

  text = ChatColor.translateAlternateColorCodes('&', text);

  var block = location.getBlock();
  var state = block.getState();

  if (!(state instanceof Sign)) throw new Error('This block is not a sign.');

  state.setLine(line, text);
  state.update();

  return null;
}
