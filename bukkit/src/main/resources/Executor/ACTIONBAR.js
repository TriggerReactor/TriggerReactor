/*******************************************************************************
 *     Copyright (C) 2018 TheBestNightSky
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

var ChatColor = Java.type("org.bukkit.ChatColor");
var Player = Java.type("org.bukkit.entity.Player");
var ChatMessageType = Java.type("net.md_5.bungee.api.ChatMessageType");
var TextComponent = Java.type("net.md_5.bungee.api.chat.TextComponent");

var validation = {
  overloads: [
    [{ type: "string", name: "message" }],
    [
      { type: Player.class, name: "player" },
      { type: "string", name: "message" },
    ],
  ],
};

function ACTIONBAR(args) {
  var p, message;

  if (overload === 0) {
    p = player;
    message = args[0];
  } else if (overload === 1) {
    p = args[0];
    message = args[1];
  }

  if (!p) return null;

  message = ChatColor.translateAlternateColorCodes("&", message);

  p.spigot().sendMessage(
    ChatMessageType.ACTION_BAR,
    new TextComponent(message)
  );

  return null;
}
