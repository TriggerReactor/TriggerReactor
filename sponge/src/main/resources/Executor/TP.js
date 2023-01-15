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
var Player = Java.type('org.spongepowered.api.entity.living.player.Player')
var Location = Java.type('org.spongepowered.api.world.Location')
var Sponge = Java.type('org.spongepowered.api.Sponge')

function TP(args) {
  var target = player,
    location

  if (args.length == 3) {
    var world = player.getWorld()
    var x = args[0]
    var y = args[1]
    var z = args[2]

    target = player
    location = new Location(world, x, y, z)
  } else if (args.length == 4) {
    var world = player.getWorld()
    var x = args[0]
    var y = args[1]
    var z = args[2]

    if (args[3] instanceof Player) target = args[3]
    else target = Sponge.getServer().getPlayer(args[3]).orElse(null)
    location = new Location(world, x, y, z)
  } else if (args.length == 1) {
    target = player
    location = args[0]
  }

  if (!(target instanceof Player)) {
    throw new Error('Could not find a player')
  } else if (!(location instanceof Location)) {
    throw new Error('Could not find a target location')
  }

  target.setLocation(location)
  return null
}
