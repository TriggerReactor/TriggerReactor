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

function SETFLYMODE(args) {
  if (args.length !== 1) {
    throw new Error('Incorrect number of arguments for executor SETFLYMODE')
  }

  var arg = args[0]

  if (!(typeof arg === 'boolean')) {
    throw new Error('Invalid argument for executor SETFLYMODE: ' + arg)
  }

  player.offer(Keys.CAN_FLY, arg)
  player.offer(Keys.IS_FLYING, arg)
}
