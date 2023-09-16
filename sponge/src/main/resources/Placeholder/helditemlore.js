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
var ArrayList = Java.type('java.util.ArrayList')

function helditemlore(args) {
  if (player == null) return null

  var inHand = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null)
  if (inHand == null) return ''

  if (args.length < 1) throw new Error('Invalid parameter! Need [Number]')

  if (typeof args[0] !== 'number')
    throw new Error("Invalid parameter! helditemlore accepts 'number' as parameter.")

  var lores = inHand.get(Keys.ITEM_LORE).orElse(new ArrayList())

  var index = args[0] | 0
  if (index < 0 || lores.size() <= index) return ''

  return lores[index].toPlain()
}
