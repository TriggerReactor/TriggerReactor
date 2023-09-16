/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
var HandTypes = Java.type('org.spongepowered.api.data.type.HandTypes')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes')
var Text = Java.type('org.spongepowered.api.text.Text')
var ArrayList = Java.type('java.util.ArrayList')

function MODIFYHELDITEM(args) {
  if (!player) return null

  if (args.length < 2) throw new Error('Invalid parameters. Need [String, Depends on type]')

  if (typeof args[0] !== 'string')
    throw new Error("Invalid parameters. First parameter wasn't a String")

  var type = args[0].toUpperCase()
  var helditem = player.getItemInHand(HandTypes.MAIN_HAND).orElse(ItemStack.of(ItemTypes.AIR, 1))

  if (type.equals('TITLE')) {
    if (helditem.getType() == ItemTypes.AIR) return null

    helditem.offer(Keys.DISPLAY_NAME, Text.of(args[1]))
  } else if (type.equals('LORE')) {
    if (args.length < 3)
      throw new Error('Invalid parameters. Need [String, String, Depends on action]')

    if (typeof args[0] !== 'string')
      throw new Error("Invalid parameters. Second parameter wasn't a String")
    var action = args[1].toUpperCase()

    if (helditem.getType() == ItemTypes.AIR) return null

    var lore = helditem.get(Keys.ITEM_LORE).orElse(new ArrayList())

    if (action.equals('ADD')) {
      var value = Text.of(args[2].toString())

      if (args.length > 3) {
        var index = args[3]
        if (typeof index !== 'number') throw new Error('index should be a number!')

        if (index > lore.size() - 1) lore.add(value)
        else lore.add(Math.max(0, Math.min(lore.size() - 1, index)), value)
      } else {
        lore.add(value)
      }
    } else if (action.equals('SET')) {
      if (args.length < 4) throw new Error('Invalid parameters. Need [String, String, Any, Number]')

      while (lore.size() - 1 < args[3]) lore.add(Text.of(''))

      lore.set(args[3], Text.of(args[2]))
    } else if (action.equals('REMOVE')) {
      var index = args[2]
      if (typeof index !== 'number') throw new Error('index should be a number!')

      if (0 <= index && index < lore.size()) {
        lore.remove(index)
      }
    } else {
      throw new Error('Unknown MODIFYHELDITEM LORE action ' + action)
    }

    helditem.offer(Keys.ITEM_LORE, lore)
  } else {
    throw new Error('Unknown MODIFYHELDITEM type ' + type)
  }

  player.setItemInHand(HandTypes.MAIN_HAND, helditem)

  return null
}
