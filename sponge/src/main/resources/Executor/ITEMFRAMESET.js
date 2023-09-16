/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
var EntityTypes = Java.type('org.spongepowered.api.entity.EntityTypes')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes')
var Location = Java.type('org.spongepowered.api.world.Location')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')

function ITEMFRAMESET(args) {
  if (args.length == 2 || args.length == 4) {
    var itemID = args[0].toUpperCase()
    var location

    if (args.length == 2) {
      location = args[1]
    } else {
      var world = player.getWorld()
      location = new Location(world, args[1], args[2], args[3])
    }

    var itemType = ReflectionUtil.getField(ItemTypes.class, null, itemID)
    var builder = ItemStack.builder().itemType(itemType).quantity(1)

    var entities = location.getExtent().getEntities()
    for (var i = 0; i < entities.size(); i++) {
      var entity = entities[i]
      if (entity.getType() != EntityTypes.ITEM_FRAME) continue

      var dist = entity.getLocation().getPosition().distance(location.getPosition())
      if (dist <= 1) {
        entity.offer(Keys.REPRESENTED_ITEM, builder.build().createSnapshot())
        break
      }
    }
  } else {
    throw new Error(
      'Invalid parameters. Need [Item<string>, Location<location or number number number>]'
    )
  }
  return null
}
