/*******************************************************************************
 *     Copyright (C) 2019 Craig White
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
var ValueContainer = Java.type('org.spongepowered.api.data.value.ValueContainer')
var Integer = Java.type('java.lang.Integer')

validation = {
  overloads: [[{ name: 'amount', type: 'number', miminum: 0, maximum: 1 }]]
}

function SETXP(args) {
  if (overload === 0) {
    var amount = args[0]

    if (!(player instanceof ValueContainer)) {
      throw new Error('Value in player does not support food (did you set it to something else?)')
    }

    var currentLevel = player.get(Keys.EXPERIENCE_LEVEL).orElse(0)
    var requiredExp
    if (currentLevel >= 0 && currentLevel <= 15) {
      requiredExp = 2 * currentLevel + 7
    } else if (currentLevel >= 16 && currentLevel <= 30) {
      requiredExp = 5 * currentLevel - 38
    } else if (currentLevel >= 31) {
      requiredExp = 9 * currentLevel - 158
    }

    player.offer(
      Keys.EXPERIENCE_SINCE_LEVEL,
      Integer.valueOf(Math.round(requiredExp * (amount - 0.02)))
    )
    return null
  }
}
