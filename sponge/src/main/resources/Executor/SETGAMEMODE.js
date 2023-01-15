/*******************************************************************************
 *     Copyright (C) 2021 wysohn
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
var GameModes = Java.type('org.spongepowered.api.entity.living.player.gamemode.GameModes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')

function SETGAMEMODE(args) {
  if (args.length !== 1) {
    throw new Error('Incorrect number of arguments for executor SETGAMEMODE')
  }

  var arg = args[0]

  if (typeof arg != 'string') {
    throw new Error('Invalid argument for Executor SETGAMEMODE: ' + arg)
  }

  if (!(player instanceof ValueContainer) || !player.supports(Keys.GAME_MODE)) {
    throw new Error(
      'value in variable player does not support gamemodes (did you set it to something else?)'
    )
  }

  try {
    var mode = ReflectionUtil.getField(GameModes.class, null, arg.toUpperCase())
  } catch (ex) {
    throw new Error('Unknown GAMEMODE value ' + arg)
  }

  player.offer(Keys.GAME_MODE, mode)
}
