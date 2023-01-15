/*******************************************************************************
 *     Copyright (C) 2019 Craig White
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
var Double = Java.type('java.lang.Double')

validation = {
  overloads: [[{ name: 'health', type: 'number', minimum: 0, maximum: 20 }]]
}

function SETHEALTH(args) {
  if (overload === 0) {
    var health = args[0]

    if (!(player instanceof ValueContainer)) {
      throw new Error('Value in player does not support health (did you set it to something else?)')
    }

    var maxHealth = player.get(Keys.MAX_HEALTH).orElse(-1)

    if (maxHealth === -1) {
      throw new Error(
        'value in variable player does not support health (did you set it to something else?)'
      )
    }

    if (health > maxHealth) {
      throw new Error(
        'Argument for Executor SETHEALTH is greater than the maximum health of: ' + maxHealth
      )
    }

    player.offer(Keys.HEALTH, Double.parseDouble(health))
    return null
  }
}
