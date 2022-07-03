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
var ChatColor = Java.type('org.bukkit.ChatColor');

var validation = {
  overloads: [[{type: 'string', name: 'message'}]],
};

function BROADCAST(args) {
  var message = args[0];

  if (Bukkit.getPluginManager().isPluginEnabled('PlaceholderAPI'))
    var PlaceholderAPI = Java.type('me.clip.placeholderapi.PlaceholderAPI');

  message = ChatColor.translateAlternateColorCodes('&', message);

  for each (var target in Bukkit.getOnlinePlayers()) {
    if (PlaceholderAPI)
      message = PlaceholderAPI.setPlaceholders(target, message);
    target.sendMessage(message);
  }

  return null;
}
