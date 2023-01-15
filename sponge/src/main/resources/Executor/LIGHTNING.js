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
var EntityTypes = Java.type('org.spongepowered.api.entity.EntityTypes')
var Location = Java.type('org.spongepowered.api.world.Location')
var Sponge = Java.type('org.spongepowered.api.Sponge')

function LIGHTNING(args) {
  if (args.length != 4) throw new Error('Invalid parameters! [String, Number, Number, Number]')

  if (
    typeof args[0] !== 'string' ||
    typeof args[1] !== 'number' ||
    typeof args[2] !== 'number' ||
    typeof args[3] !== 'number'
  )
    throw new Error('Invalid parameters! [String, Number, Number, Number]')

  var world = Sponge.getServer().getWorld(args[0]).orElse(null)
  if (world == null) throw new Error('Unknown world named ' + args[0])

  var location = new Location(world, args[1], args[2], args[3])
  var lightning = world.createEntity(EntityTypes.LIGHTNING, location.getPosition())
  world.spawnEntity(lightning)
}
