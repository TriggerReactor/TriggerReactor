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
var EnchantmentTypes = Java.type('org.spongepowered.api.item.enchantment.EnchantmentTypes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')
var ArrayList = Java.type('java.util.ArrayList')

function helditemhasenchant(args) {
  if (player == null) return null

  var inHand = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null)
  if (inHand == null) return false

  if (args.length < 1) throw new Error('Invalid parameter! [String]')

  if (typeof args[0] !== 'string')
    throw new Error("Invalid parameter! helditemhasenchant accepts 'String' as first paramter.")

  var ench = ReflectionUtil.getField(EnchantmentTypes.class, null, args[0].toUpperCase())
  var level = 0

  if (args.length != 1) {
    if (typeof args[1] !== 'number')
      throw new Error("Invalid parameter! helditemhasenchant accepts 'Number' as second paramter.")

    level = Math.max(0, args[1])
  }

  var enchs = inHand.get(Keys.ITEM_ENCHANTMENTS).orElse(new ArrayList())

  for (var iter = enchs.iterator(); iter.hasNext(); ) {
    var ench = iter.next()

    if (ench == ench) {
      if (level == 0) {
        return true
      } else {
        return level == ench.getLevel()
      }
    }
  }

  return false
}
