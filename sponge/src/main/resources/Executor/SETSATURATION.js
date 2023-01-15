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

validation = {
  overloads: [[{ name: 'saturation', type: 'number', minimum: 0 }]]
}

function SETSATURATION(args) {
  if (overload === 0) {
    var saturation = args[0]

    if (!(typeof saturation == 'number')) {
      throw new Error('Invalid argument for SETSATURATION: ' + saturation)
    }

    if (!(player instanceof ValueContainer)) {
      throw new Error(
        'Value in player does not support saturation (did you set it to something else?)'
      )
    }

    var bounded = player.get(Keys.SATURATION).orElse(-1)

    if (bounded === -1) {
      throw new Error(
        'value in variable player does not support saturation (did you set it to something else?)'
      )
    }

    saturation *= 1.0 //cast arg to double

    player.offer(Keys.SATURATION, saturation)
    return null
  }
}
