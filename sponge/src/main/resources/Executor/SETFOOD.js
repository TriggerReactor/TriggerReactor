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

function SETFOOD(args) {
  if (args.length !== 1) {
    throw new Error('Incorrect Number of arguments for Executor SETFOOD')
  }

  var arg = args[0]

  if (!(typeof arg === 'number')) {
    throw new Error('Invalid argument for Executor SETFOOD: ' + arg)
  }

  var rounded = Math.round(args[0])

  if (rounded !== arg) {
    throw new Error('Argument for Executor SETFOOD should be a whole number')
  }

  if (!(player instanceof ValueContainer)) {
    throw new Error('Value in player does not support food (did you set it to something else?)')
  }

  var bounded = player.getValue(Keys.FOOD_LEVEL).orElse(null)

  if (bounded === null) {
    throw new Error('Value in player does not support food (did you set it to something else?)')
  }

  if (arg < bounded.getMinValue()) {
    throw new Error(
      'argument for executor SETFOOD is too low: ' + arg + ', minimum is: ' + bounded.getMinValue()
    )
  }

  if (arg > bounded.getMaxValue()) {
    throw new Error(
      'argument for executor SETFOOD is too high: ' + arg + ', maximum is: ' + bounded.getMaxValue()
    )
  }

  player.offer(Keys.FOOD_LEVEL, arg)
}
