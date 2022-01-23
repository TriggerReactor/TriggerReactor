/*******************************************************************************
 *     Copyright (C) 2019 Craig White
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

validation = {
  overloads: [[{ name: 'maxhealth', type: 'number', minimum: 1 }]]
}

function SETMAXHEALTH(args) {
  if (overload === 0) {
    var maxhealth = args[0]

    if (!(player instanceof ValueContainer)) {
      throw new Error(
        'Value in player does not support max health (did you set it to something else?)'
      )
    }

    var bounded = player.get(Keys.MAX_HEALTH).orElse(-1)

    if (bounded === -1) {
      throw new Error(
        'value in variable player does not support max health (did you set it to something else?)'
      )
    }

    maxhealth *= 1.0 //cast to double

    player.offer(Keys.MAX_HEALTH, maxhealth)
    return null
  }
}
