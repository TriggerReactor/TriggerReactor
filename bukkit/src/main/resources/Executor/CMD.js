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
var PlayerCommandPreprocessEvent = Java.type(
  'org.bukkit.event.player.PlayerCommandPreprocessEvent'
);

var validation = {
  overloads: [[{ type: 'string', name: 'command' }]]
};

function CMD(args) {
  if (!player) throw new Error('Player is null.');

  var command = args[0];

  var event = new PlayerCommandPreprocessEvent(player, '/' + command);

  Bukkit.getPluginManager().callEvent(event);

  if (!event.isCancelled()) Bukkit.dispatchCommand(player, command);

  return null;
}
