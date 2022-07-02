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

var Bukkit = Java.type("org.bukkit.Bukkit");
var ItemStack = Java.type("org.bukkit.inventory.ItemStack");
var Material = Java.type("org.bukkit.Material");
var Location = Java.type("org.bukkit.Location");

var validation = {
  overloads: [
    [{ type: Location.class, name: "location" }],
    [
      { type: Location.class, name: "location" },
      { type: "number", name: "power" },
    ],
    [
      { type: "number", name: "x" },
      { type: "number", name: "y" },
      { type: "number", name: "z" },
    ],
    [
      { type: "number", name: "x" },
      { type: "number", name: "y" },
      { type: "number", name: "z" },
      { type: "number", name: "power" },
    ],
  ],
};

function EXPLOSION(args) {
  var location, power;

  if (overload === 0) {
    location = args[0];
    power = 4;
  } else if (overload === 1) {
    location = args[0];
    power = args[1];
  } else if (overload === 2) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    power = 4;
  } else if (overload === 3) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    power = args[3];
  }

  location.getWorld().createExplosion(location, power);

  return null;
}
