/*******************************************************************************
 *     Copyright (C) 2017 soliddanii, wysohn
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
    [
      { type: ItemStack.class, name: "itemStack" },
      { type: Location.class, name: "location" },
    ],
    [
      { type: ItemStack.class, name: "itemStack" },
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: Location.class, name: "location" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: "int", name: "amount" },
      { type: Location.class, name: "location" },
    ],
    [
      { type: "string", name: "materialName" },
      { type: "int", name: "amount" },
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
    ],
  ],
};

function DROPITEM(args) {
  var itemStack, location;

  if (overload === 0) {
    itemStack = args[0];
    location = args[1];
  } else if (overload === 1) {
    itemStack = args[0];
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[1],
      args[2],
      args[3]
    );
  } else if (overload === 2) {
    itemStack = new ItemStack(Material.valueOf(args[0]));
    location = args[1];
  } else if (overload === 3) {
    itemStack = new ItemStack(Material.valueOf(args[0]));
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[1],
      args[2],
      args[3]
    );
  } else if (overload === 4) {
    itemStack = new ItemStack(Material.valueOf(args[0]), args[1]);
    location = args[2];
  } else if (overload === 5) {
    itemStack = new ItemStack(Material.valueOf(args[0]), args[1]);
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[2],
      args[3],
      args[4]
    );
  }

  location.getWorld().dropItem(location, itemStack);

  return null;
}
