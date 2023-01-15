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
var Keys = Java.type('org.spongepowered.api.data.key.Keys')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var TextUtil = Java.type('io.github.wysohn.triggerreactor.sponge.tools.TextUtil')
var String = Java.type('java.lang.String')

validation = {
  overloads: [
    [
      { name: 'name', type: 'string' },
      { name: 'item', type: ItemStack.class }
    ]
  ]
}

function SETITEMNAME(args) {
  if (overload === 0) {
    var item = args[1]
    var name = TextUtil.colorStringToText(String.valueOf(args[0]))
    if (item.getType().name().toLowerCase().equals('minecraft:air') || item == null) {
      return null
    } else {
      item.offer(Keys.DISPLAY_NAME, name)
      return null
    }
  }
}
