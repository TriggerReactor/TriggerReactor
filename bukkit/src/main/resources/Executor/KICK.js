/*******************************************************************************
 *     Copyright (C) 2019 wysohn
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

var Player = Java.type('org.bukkit.entity.Player');
var ChatColor = Java.type('org.bukkit.ChatColor');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'reason' }
    ],
    [
      { type: Player.class, name: 'player' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'reason' }
    ]
  ]
};

function KICK(args) {
  var target;
  var reason;

  if (overload === 0) {
    target = player;
    reason = args[0];
  } else if (overload === 1) {
    target = args[0];
    reason = "&c[TR] You've been kicked from the server.";
  } else if (overload === 2) {
    target = args[0];
    reason = args[1];
  }

  if (!target) throw new Error('Player is null.');

  reason = ChatColor.translateAlternateColorCodes('&', reason);

  target.kickPlayer(reason);

  return null;
}
