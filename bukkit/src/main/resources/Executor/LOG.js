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

var Dependency = Java.type('io.github.wysohn.triggerreactor.core.main.Dependency');
var Platform = Java.type('io.github.wysohn.triggerreactor.core.main.Platform');
var Bukkit = Java.type('org.bukkit.Bukkit');
var ChatColor = Java.type('org.bukkit.ChatColor');
var Object = Java.type('java.lang.Object');

var validation = {
  overloads: [
    [
      { type: Object.class, name: 'message' }
    ]
  ]
};

function LOG(args) {
  var message = args[0].toString();
  message = ChatColor.translateAlternateColorCodes('&', message);

  var platform = plugin.getPlatform();
  if (platform.supports(Dependency.MiniMessage)) {
    var mm = Java.type('net.kyori.adventure.text.minimessage.MiniMessage').miniMessage();
    message = mm.deserialize(message);
  }

  Bukkit.getConsoleSender().sendMessage(message);

  return null;
}
