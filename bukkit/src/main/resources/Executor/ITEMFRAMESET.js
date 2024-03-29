/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
var ItemFrame = Java.type('org.bukkit.entity.ItemFrame')
var Location = Java.type('org.bukkit.Location');

var validation = {
  overloads: [
    [
      { type: ItemStack.class, name: 'itemStack' },
      { type: Location.class, name: 'location' },
    ],
    [
      { type: ItemStack.class, name: 'itemStack' },
      { type: 'int', name: 'x' },
      { type: 'int', name: 'y' },
      { type: 'int', name: 'z' },
    ],
  ],
};

function ITEMFRAMESET(args) {
  var itemStack, location;

  if (overload === 0) {
    itemStack = args[0];
    location = args[1];
  } else if (overload === 1) {
    itemStack = args[0];
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld('world'),
      args[1],
      args[2],
      args[3]
    );
  }

  var entities = location.getWorld().getNearbyEntities(location, 1, 1, 1);
  var iter = entities.iterator();
  while (iter.hasNext()) {
    var entity = iter.next();

    if (entity instanceof ItemFrame) {
      entity.setItem(itemStack);
    }
  }

  return null;
}
