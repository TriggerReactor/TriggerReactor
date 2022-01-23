/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
var Sponge = Java.type('org.spongepowered.api.Sponge')
var Player = Java.type('org.spongepowered.api.entity.living.player.Player')
var Text = Java.type('org.spongepowered.api.text.Text')

function CLEARCHAT(args) {
  var target
  if (args.length === 0) {
    target = player
  } else if (args.length === 1) {
    if (args[0] instanceof Player) {
      target = args[0]
    } else {
      target = Sponge.getGame().getServer().getPlayer(args[0]).orElse(null)
    }
  } else if (args.length >= 2) {
    throw new Error('Too many parameters found! CLEARCHAT accept up to one parameter.')
  }

  if (!target) {
    throw new Error('Player not found.')
  }

  for (var i = 0; i < 30; i++) {
    target.sendMessage(Text.of(''))
  }

  return null
}
