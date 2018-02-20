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
		var item = args[0];
		var location = args[1];

		location.getWorld().dropItem(location, item);

	}else if (args.length == 4 || args.length == 6) {
		var itemID = args[0];
		var amount = args[1];
		var enchan = args[2];
		var location;

		if(args.length == 4){
			location = args[3];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[3], args[4], args[5]);
		}

		var Enchantment = Java.type('org.bukkit.enchantments.Enchantment');
		var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
		Block = location.getBlock();

		if(typeof itemID==='number' && (itemID%1)===0){
			ItemStack = new ItemStack(itemID, amount);
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someItem = Material.valueOf(itemID.toUpperCase());
			ItemStack = new ItemStack(someItem, amount);
		}

		if(enchan.toUpperCase() !== 'NONE'){
			var encharg = enchan.split(',');
			for each (en in encharg){
				var ench = Enchantment.getByName(en.split(':')[0].toUpperCase());
				var level = parseInt(en.split(':')[1]);

				if(!ench)
					throw Error(en.split(':')[0]+" is not a valid Enchantment.");

				ItemStack.addUnsafeEnchantment(ench, level);
			}
		}

		location.getWorld().dropItem(location, ItemStack);

	}else if(args.length == 5 || args.length == 7){ 
		var itemID = args[0];
		var itemData = args[1];
		var amount = args[2];
		var enchan = args[3];
		var location;

		if(args.length == 5){
			location = args[4];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[4], args[5], args[6]);
		}

		var Enchantment = Java.type('org.bukkit.enchantments.Enchantment');
		var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
		Block = location.getBlock();

		if(typeof itemID==='number' && (itemID%1)===0){
			ItemStack = new ItemStack(itemID, amount, 0, itemData);
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someItem = Material.valueOf(itemID.toUpperCase());
			ItemStack = new ItemStack(someItem, amount, 0, itemData);
		}

		if(enchan.toUpperCase() !== 'NONE'){
			var encharg = enchan.split(',');
			for each (en in encharg){
				var ench = Enchantment.getByName(en.split(':')[0].toUpperCase());
				var level = parseInt(en.split(':')[1]);

				if(!ench)
					throw Error(en.split(':')[0]+" is not a valid Enchantment.");

				ItemStack.addUnsafeEnchantment(ench, level);
			}
		}

		location.getWorld().dropItem(location, ItemStack);

	}else {
		throw new Error(
			'Invalid parameters. Need [Item, Location or Item<string or number>, Quantity<number>, Enchantments<string>, Location<location or number number number>]');
	}
	return null;
}