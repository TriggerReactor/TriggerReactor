/*******************************************************************************
 *     Copyright (C) 2017 soliddanii, wysohn
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
 function DROPITEM(args) {
	if (args.length == 2){
		var IS = args[0];
		var location = args[1];
		var world = location.getExtent();

        var item = world.createEntity(EntityTypes.ITEM, location.getPosition());
        item.offer(Keys.REPRESENTED_ITEM, IS.createSnapshot());
		world.spawnEntity(item);
	}else if (args.length == 4 || args.length == 6) {
		var itemID = args[0].toUpperCase();
		var amount = args[1];
		var enchan = args[2];
		var location;
		var world = player.getWorld();

		if(args.length == 4){
			location = args[3];
		}else{   
			location = new Location(world, args[3], args[4], args[5]);
		}

		var itemType = ReflectionUtil.getField(ItemTypes.class, null, itemID);
		var builder = ItemStack.builder().itemType(itemType).quantity(amount);

		if(enchan.toUpperCase() !== 'NONE'){
			var encharg = enchan.split(',');
			for each (en in encharg){
				var ench = ReflectionUtil.getField(EnchantmentTypes.class, null, en.split(':')[0])
				var level = parseInt(en.split(':')[1]);

				if(!ench)
					throw Error(en.split(':')[0]+" is not a valid Enchantment.");

				var ArrayList = Java.type('java.util.ArrayList');
				var enchs = new ArrayList();
				enchs.add(Enchantment.of(ench, level));
				builder.add(Keys.ITEM_ENCHANTMENTS, enchs);
			}
		}

        var item = world.createEntity(EntityTypes.ITEM, location.getPosition());
        item.offer(Keys.REPRESENTED_ITEM, builder.build().createSnapshot());
		world.spawnEntity(item);
	} else {
		throw new Error(
			'Invalid parameters. Need [Item, Vector3i or Item<string>, Quantity<number>, Enchantments<string>, Location<Vector3i or number number number>]');
	}
	return null;
}