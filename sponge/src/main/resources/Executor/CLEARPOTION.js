/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
var PotionEffectTypes = Java.type('org.spongepowered.api.effect.potion.PotionEffectTypes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')

function CLEARPOTION(args) {
  if (!player) return null
  var potionEffectList = player.get(Keys.POTION_EFFECTS).orElse(null)
  if (!potionEffectList) return null

  if (args.length === 0) {
    potionEffectList.clear()
  } else if (args.length === 1) {
    var typeName = args[0].toUpperCase()
    var type = ReflectionUtil.getField(PotionEffectTypes.class, null, typeName)
    if (type === null) return null

    var iter = potionEffectList.iterator()
    while (iter.hasNext()) {
      var effect = iter.next()
      if (effect.getType() === type) {
        iter.remove()
      }
    }
  } else {
    throw new Error('Invalid arguments')
  }

  player.offer(Keys.POTION_EFFECTS, potionEffectList)
  return null
}
