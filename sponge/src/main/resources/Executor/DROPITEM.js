/*******************************************************************************
 *     Copyright (C) 2017 soliddanii, wysohn
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
var EntityTypes = Java.type('org.spongepowered.api.entity.EntityTypes')
var Enchantment = Java.type('org.spongepowered.api.item.enchantment.Enchantment')
var EnchantmentTypes = Java.type('org.spongepowered.api.item.enchantment.EnchantmentTypes')
var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
var Location = Java.type('org.spongepowered.api.world.Location')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')
var ArrayList = Java.type('java.util.ArrayList')

function DROPITEM(args) {
  if (args.length === 2) {
    var itemStack = args[0]
    var location = args[1]
    var world = location.getExtent()

    var item = world.createEntity(EntityTypes.ITEM, location.getPosition())
    item.offer(Keys.REPRESENTED_ITEM, itemStack.createSnapshot())
    world.spawnEntity(item)
  } else if (args.length == 4 || args.length == 6) {
    var itemID = args[0].toUpperCase()
    var quantity = args[1]
    var enchantRaw = args[2]
    var location
    var world = player.getWorld()

    if (args.length == 4) {
      location = args[3]
    } else {
      location = new Location(world, args[3], args[4], args[5])
    }

    var itemType = ReflectionUtil.getField(ItemTypes.class, null, itemID)
    var builder = ItemStack.builder().itemType(itemType).quantity(quantity)
    var enchants = enchantRaw.replace(/\s+/, '').split(',')
    var enchantmentList = new ArrayList()
    enchants.forEach(function (enchant) {
      var enchantName = enchant.split(':')[0]
      var enchantLevel = enchant.split(':')[1]
      var enchantType = ReflectionUtil.getField(EnchantmentTypes.class, null, enchantName)

      if (!enchantType) {
        throw new Error(enchantName + ' is not a valid Enchantment.')
      }

      enchantmentList.add(Enchantment.of(enchantName, enchantLevel))
    })

    if (enchantmentList.size() > 0) {
      builder.add(Keys.ITEM_ENCHANTMENTS, enchantmentList)
    }

    var item = world.createEntity(EntityTypes.ITEM, location.getPosition())
    item.offer(Keys.REPRESENTED_ITEM, builder.build().createSnapshot())
    world.spawnEntity(item)
  } else {
    throw new Error(
      'Invalid parameters. Need [Item, Vector3i or Item<string>, Quantity<number>, Enchantments<string>, Location<Vector3i or number number number>]'
    )
  }

  return null
}
