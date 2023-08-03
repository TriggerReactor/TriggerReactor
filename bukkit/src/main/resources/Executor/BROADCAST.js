/*******************************************************************************
 *     Copyright (c) 2023 TriggerReactor Team
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

var Dependency = Java.type(
  "io.github.wysohn.triggerreactor.core.main.Dependency"
);
var Platform = Java.type("io.github.wysohn.triggerreactor.core.main.Platform");
var PlatformManager = Java.type(
  "io.github.wysohn.triggerreactor.core.manager.PlatformManager"
);
var BukkitUtil = Java.type(
  "io.github.wysohn.triggerreactor.bukkit.tools.BukkitUtil"
);
var Bukkit = Java.type("org.bukkit.Bukkit");
var ChatColor = Java.type("org.bukkit.ChatColor");
var Object = Java.type("java.lang.Object");

var validation = {
  overloads: [[{ type: Object.class, name: "message" }]],
};

function BROADCAST(args) {
  var PlaceholderAPI;
  var message = args[0].toString();

  message = ChatColor.translateAlternateColorCodes("&", message);

  if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
    PlaceholderAPI = Java.type("me.clip.placeholderapi.PlaceholderAPI");
  }

  var platform = injector.getInstance(PlatformManager.class).current();
  if (platform.supports(Dependency.MiniMessage)) {
    var mm = Java.type(
      "net.kyori.adventure.text.minimessage.MiniMessage"
    ).miniMessage();
    message = mm.deserialize(message);
  }

  var players = BukkitUtil.getOnlinePlayers();
  var iter = players.iterator();
  while (iter.hasNext()) {
    var target = iter.next();
    var msg = PlaceholderAPI
      ? PlaceholderAPI.setPlaceholders(target, message)
      : message;

    target.sendMessage(msg);
  }

  return null;
}
