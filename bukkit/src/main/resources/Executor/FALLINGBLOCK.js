/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
 *	   Copyright (C) 2022 Ioloolo
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
var Material = Java.type("org.bukkit.Material");
var Location = Java.type("org.bukkit.Location");

var validation = {
  overloads: [
    [
      { type: Material.class, name: "material" },
      { type: Location.class, name: "location" },
    ],
    [
      { type: Material.class, name: "material" },
      { type: "number", name: "x" },
      { type: "number", name: "y" },
      { type: "number", name: "z" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: Location.class, name: "location" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: "number", name: "x" },
      { type: "number", name: "y" },
      { type: "number", name: "z" },
    ],
  ],
};

function FALLINGBLOCK(args) {
  var material, location;

  if (overload === 0) {
    material = args[0];
    location = args[1];
  } else if (overload === 1) {
    material = args[0];
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[1],
      args[2],
      args[3]
    );
  } else if (overload === 2) {
    material = Material.valueOf(args[0]);
    location = args[1];
  } else if (overload === 3) {
    material = Material.valueOf(args[0]);
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[1],
      args[2],
      args[3]
    );
  }

  location.getWorld().spawnFallingBlock(location, material, 0);

  return null;
}
