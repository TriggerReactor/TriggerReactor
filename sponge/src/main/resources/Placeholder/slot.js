/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
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
var InteractInventoryEvent = Java.type(
  'org.spongepowered.api.event.item.inventory.InteractInventoryEvent'
)
var ClickInventoryEvent = Java.type(
  'org.spongepowered.api.event.item.inventory.ClickInventoryEvent'
)
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes')

validation = {
  overloads: [[{ name: 'index', type: 'int' }]]
}

function slot(args) {
  if (
    event instanceof InteractInventoryEvent.Open ||
    event instanceof ClickInventoryEvent ||
    event instanceof InteractInventoryEvent.Close
  ) {
    if (args[0] < 0 || args[0] >= inventory.capacity())
      throw new Error('Unexpected token: slot number should be at least 0, up to its size.')
    else {
      var y = args[0] / 9
      var x = args[0] % 9

      return inventory.peek(x, y).orElse(ItemStack.builder().itemType(ItemTypes.AIR).build())
    }
  } else {
    throw new Error('$slot Placeholder is available only in InventoryTrigger!')
  }
}
