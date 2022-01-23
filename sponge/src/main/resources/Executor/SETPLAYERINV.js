/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
 *     Copyright (C) 2022 Sayakie
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
var QueryOperationTypes = Java.type(
  'org.spongepowered.api.item.inventory.query.QueryOperationTypes'
)
var MainPlayerInventory = Java.type(
  'org.spongepowered.api.item.inventory.entity.MainPlayerInventory'
)
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')

validation = {
  overloads: [
    [
      { name: 'slot', type: 'int', minimum: 0, maximum: 44 },
      { name: 'itemStack', type: ItemStack.class }
    ]
  ]
}

function SETPLAYERINV(args) {
  if (player == null) return null

  if (overload === 0) {
    var itemStack = args[1]

    var carriedInv = player.getInventory()
    var grids = carriedInv.query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))

    var y = args[0] / 9
    var x = args[0] % 9

    var result = grids.set(x, y, itemStack)
    //should we return result?

    return null
  }
}
