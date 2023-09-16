/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')

function SPAWN(args) {
  var world = player.getWorld()

  if (args.length == 1) {
    var location = player.getLocation()
    var entityType = ReflectionUtil.getField(EntityTypes.class, null, args[0].toUpperCase())
    var entity = world.createEntity(entityType, location.getPosition())

    world.spawnEntity(entity)
  } else if (args.length == 2) {
    var location = args[0]
    var entityType = ReflectionUtil.getField(EntityTypes.class, null, args[1].toUpperCase())
    var entity = world.createEntity(entityType, location.getPosition())

    world.spawnEntity(entity)
  } else {
    throw new Error('Invalid parameters. Need [String] or [Location, String]')
  }
}
