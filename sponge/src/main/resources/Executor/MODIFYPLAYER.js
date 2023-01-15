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
var GameModes = Java.type('org.spongepowered.api.entity.living.player.gamemode.GameModes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')
GameModes.SURVIVAL
function MODIFYPLAYER(args) {
  if (!player) return null

  if (args.length != 2) throw new Error('Invalid parameters. Need [String, Depends on type]')

  if (typeof args[0] !== 'string')
    throw new Error("Invalid parameters. First parameter wasn't a String")

  var type = args[0].toUpperCase()
  var value = args[1]

  switch (type) {
    case 'HEALTH':
      player.offer(Keys.HEALTH, value)
      break
    case 'FOOD':
      player.offer(Keys.FOOD_LEVEL, value)
      break
    case 'SATURATION':
      player.offer(Keys.SATURATION, value)
      break
    case 'EXP':
      player.offer(Keys.EXPERIENCE_SINCE_LEVEL, value)
      break
    case 'WALKSPEED':
      player.offer(Keys.WALKING_SPEED, value)
      break
    case 'FLYSPEED':
      player.offer(Keys.FLYING_SPEED, value)
      break
    case 'FLY':
      player.offer(Keys.CAN_FLY, value)
      break
    case 'GAMEMODE':
      try {
        var mode = ReflectionUtil.getField(GameModes.class, null, value.toUpperCase())
        player.offer(Keys.GAME_MODE, mode)
      } catch (ex) {
        throw new Error('Unknown GAEMMODE value ' + value)
      }
      break
    case 'MAXHEALTH':
      player.offer(Keys.MAX_HEALTH, value)
      break
    default:
      var key = ReflectionUtil.getField(Keys.class, null, type)
      player.offer(key, value)
  }

  return null
}
