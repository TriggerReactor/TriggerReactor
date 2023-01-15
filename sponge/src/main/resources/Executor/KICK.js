/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
var Player = Java.type('org.spongepowered.api.entity.living.player.Player')
var Text = Java.type('org.spongepowered.api.text.Text')
var Sponge = Java.type('org.spongepowered.api.Sponge')
var TextUtil = Java.type('io.github.wysohn.triggerreactor.sponge.tools.TextUtil')
var String = Java.type('java.lang.String')

function KICK(args) {
  var target = player,
    message = TextUtil.colorStringToText("&c[TR] You've been kicked from the server.")

  if (args.length === 0) {
    if (!target) {
      throw new Error(
        'Too few arguments! You should enter at least one argument if you use KICK executor from console.'
      )
    }
  } else if (args.length === 1) {
    if (args[0] instanceof Player) {
      target = args[0]
    } else {
      var targetable = Sponge.getGame().getServer().getPlayer(args[0]).orElse(null)

      if (targetable) {
        target = targetable
      } else {
        message = args[0]
      }
    }
  } else if (args.length === 2) {
    target = args[0]
    message = args[1]
  } else if (args.length > 2) {
    throw new Error('Too many arguments! KICK Executor accepts up to two arguments.')
  }

  if (!(target instanceof Player)) {
    target = Sponge.getGame().getServer().getPlayer(args[0]).orElse(null)

    if (!target) throw new Error('Found unexpected type of argument(s) - player: ' + target)
  }

  if (!(message instanceof Text)) {
    message = TextUtil.colorStringToText(String.valueOf(message))
  }

  target.kick(message)
  return null
}
