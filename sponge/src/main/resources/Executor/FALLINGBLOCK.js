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
var BlockTypes = Java.type('org.spongepowered.api.block.BlockTypes')
var Keys = Java.type('org.spongepowered.api.data.key.Keys')
var GravityAffectedProperty = Java.type(
  'org.spongepowered.api.data.property.block.GravityAffectedProperty'
)
var EntityTypes = Java.type('org.spongepowered.api.entity.EntityTypes')
var Location = Java.type('org.spongepowered.api.world.Location')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')

function FALLINGBLOCK(args) {
  if (args.length == 2 || args.length == 4) {
    var blockID = args[0]
    var location

    var blockType = ReflectionUtil.getField(BlockTypes.class, null, blockID)

    if (args.length == 2) {
      location = args[1]
    } else {
      var world = player.getWorld()
      location = new Location(world, args[1], args[2], args[3])
    }

    var propertyVal = blockType.getProperty(GravityAffectedProperty.class).orElse(false)

    if (propertyVal) {
      var world = location.getExtent()
      var fallingBlock = world.createEntity(EntityTypes.FALLING_BLOCK, location.getPosition())
      fallingBlock.offer(Keys.FALLING_BLOCK_STATE, blockType.getDefaultState())
      fallingBlock.offer(Keys.CAN_PLACE_AS_BLOCK, true)
      fallingBlock.offer(Keys.FALL_TIME, 1)
      world.spawnEntity(fallingBlock)
    }
  } else {
    throw new Error(
      'Invalid parameters. Need [Block<string>, Location<location or number number number>]'
    )
  }

  return null
}
