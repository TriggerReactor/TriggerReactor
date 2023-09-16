/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
var Sponge = Java.type('org.spongepowered.api.Sponge')
var Integer = Java.type('java.lang.Integer')

function BURN(args) {
  var target, seconds

  if (args.length === 1) {
    if (typeof args[0] !== 'number') {
      throw new Error('Invalid number for seconds to burn: ' + args[0])
    } else if (args[0] < 0) {
      throw new Error('The number of seconds to burn should be positive')
    }

    target = player
    seconds = Math.min(args[0] * 20, Integer.MAX_VALUE)
  } else if (args.length === 2) {
    target = args[0]

    if (typeof args[0] === 'string') {
      target = Sponge.getServer().getPlayer(target).orElse(null)
    }

    if (target === null) {
      throw new Error('Player to burn does not exist.')
    } else if (typeof args[1] !== 'number') {
      throw new Error('Invalid number for seconds to burn: ' + target.getName())
    } else if (args[1] < 0) {
      throw new Error('The number of seconds to burn should be positive.')
    }
  } else {
    throw new Error('Invalid parameters. Need [Number] or [Entity or String, Number]')
  }

  target.offer(Keys.FIRE_TICKS, Math.round(seconds))
  return null
}
