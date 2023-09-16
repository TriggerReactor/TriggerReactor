/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
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

var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
var Material = Java.type('org.bukkit.Material');

var validation = {
  overloads: [
    [
      { type: 'int', minimum: 0, maximum: 35, name: 'slot' }
    ]
  ]
}

function playerinv(args) {
  if (!player)
    throw new Error('Player is null.');

  var slot = args[0];
  var item = player.getInventory().getItem(slot);

  if (!item)
    item = new ItemStack(Material.AIR);

  return item;
}