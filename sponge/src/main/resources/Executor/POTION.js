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
var PotionEffect = Java.type('org.spongepowered.api.effect.potion.PotionEffect')
var PotionEffectTypes = Java.type('org.spongepowered.api.effect.potion.PotionEffectTypes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')
var ArrayList = Java.type('java.util.ArrayList')

function POTION(args) {
  if (player === null) return null

  if (args.length < 2) throw new Error('Invalid parameters. Need [String, Depends on type]')

  if (typeof args[0] !== 'string')
    throw new Error("Invalid parameters. First parameter wasn't a String")

  var typeName = args[0].toUpperCase()
  var type = ReflectionUtil.getField(PotionEffectTypes.class, null, typeName)

  if (typeof args[1] !== 'number') throw new Error('Second parameter should be a number.')

  var level = 1
  if (args.length > 2) {
    if (typeof args[2] != 'number') throw new Error('Third parameter should be a number')
    else level = args[2]
  }

  var list = player.get(Keys.POTION_EFFECTS).orElse(new ArrayList())

  list.add(
    PotionEffect.builder()
      .potionType(type)
      .amplifier(level - 1)
      .duration(args[1])
      .build()
  )
  player.offer(Keys.POTION_EFFECTS, list)
  return null
}
