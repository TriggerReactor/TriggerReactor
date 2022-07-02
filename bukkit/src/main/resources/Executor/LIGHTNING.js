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

var Bukkit = Java.type("org.bukkit.Bukkit");
var Location = Java.type("org.bukkit.Location");

var validation = {
  overloads: [
    [{ type: Location.class, name: "location" }],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
    ],
  ],
};

function LIGHTNING(args) {
  var location;

  if (overload === 0) location = args[0];
  else if (overload === 1)
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );

  location.getWorld().strikeLightning(location);

  return null;
}
