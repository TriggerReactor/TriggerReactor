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
var Keys = Java.type('org.spongepowered.api.data.key.Keys')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var TextUtil = Java.type('io.github.wysohn.triggerreactor.sponge.tools.TextUtil')
var String = Java.type('java.lang.String')
var ArrayList = Java.type('java.util.ArrayList')

validation = {
  overloads: [
    [
      { name: 'lore', type: 'string' },
      { name: 'item', type: ItemStack.class }
    ]
  ]
}

function SETITEMLORE(args) {
  var combinedString = args[0]
  var item = args[1]
  var lores = combinedString.split('\n')
  var loreList = new ArrayList()
  for (var k = 0; k < lores.length; k++) {
    loreList.add(k, TextUtil.colorStringToText(String.valueOf(lores[k])))
  }
  if (item.getType().getName().toLowerCase().equals('minecraft:air') || item == null) {
    return null
  } else {
    item.offer(Keys.ITEM_LORE, loreList)
    return null
  }
}
