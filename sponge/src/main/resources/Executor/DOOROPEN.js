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
var Location = Java.type('org.spongepowered.api.world.Location')

function DOOROPEN(args) {
  if (args.length == 1 || args.length == 3) {
    var location

    if (args.length == 1) {
      location = args[0]
    } else {
      var world = player.getWorld()
      location = new Location(world, args[0], args[1], args[2])
    }

    if (!(location && location instanceof Location)) {
      throw new Error('Invalid location')
    }

    var current = location.get(Keys.OPEN).orElse(null)
    if (current == null) throw new Error('Block at ' + location + " doesn't have OPEN trait!")

    location.offer(Keys.OPEN, true)
    return null
  } else {
    throw new Error('Invalid parameters. Need [Location] or [number, number, number]')
  }
}
