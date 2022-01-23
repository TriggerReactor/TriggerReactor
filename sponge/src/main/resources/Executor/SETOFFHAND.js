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
var HandTypes = Java.type('org.spongepowered.api.data.type.HandTypes')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')

validation = {
  overloads: [[{ name: 'item', type: ItemStack.class }]]
}

function SETOFFHAND(args) {
  if (overload === 0) {
    if (player == null) return null

    var item = args[0]
    if (item == null || ItemStack.empty().equalTo(item)) return null

    player.setItemInHand(HandTypes.OFF_HAND, item)
    return null
  }
}
