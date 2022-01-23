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

validation = {
  overloads: [[{ name: 'speed', type: 'number' }]]
}

function SETFLYSPEED(args) {
  if (overload === 0) {
    var speed = args[0]

    if (speed < -1 || speed > 1) {
      throw new Error('Argument for Executor SETFLYSPEED is outside of range -1..1')
    }

    speed *= 1.0 //convert arg to double

    player.offer(Keys.FLYING_SPEED, speed)
    return null
  }
}
