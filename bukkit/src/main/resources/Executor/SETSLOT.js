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
var InventoryEvent = Java.type('org.bukkit.event.inventory.InventoryEvent');

var validation = {
  overloads: [
    [
      { name: 'index', type: 'int', minimum: 0 },
      { name: 'item', type: ItemStack.class }
    ]
  ]
};

function SETSLOT(args) {
  if (!(event instanceof InventoryEvent))
    throw new Error('#SETSLOT is available only in InventoryTrigger.');

  var index = args[0];
  var item = args[1];
  var inventory = event.getInventory();

  if (index >= inventory.getSize())
    throw new Error(
      'Unexpected token: slot number should be at least 0, up to its size.'
    );

  inventory.setItem(index, item);

  return null;
}
