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
 function ITEMFRAMESET(args) {
	if (args.length == 2 || args.length == 4) {
		var itemID = args[0];
		var location;

		if(args.length == 2){
			location = args[1];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[1], args[2], args[3]);
		}

		var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
		Block = location.getBlock();

		if(typeof itemID==='number' && (itemID%1)===0){
			for each (Entity in Block.getWorld().getNearbyEntities(Block.getLocation(), 2, 2, 2)){
				if(typeof Entity.setItem == 'function'){
					Entity.setItem(new ItemStack(itemID, 1, 0));
				}
			}
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someBlock = Material.valueOf(itemID.toUpperCase());
			for each (Entity in Block.getWorld().getNearbyEntities(Block.getLocation(), 2, 2, 2)){
				if(typeof Entity.setItem == 'function'){
					Entity.setItem(new ItemStack(someBlock, 1, 0));
				}
			}
		}


	}else if(args.length == 3 || args.length == 5){ 
		var itemID = args[0];
		var itemData = args[1];
		var location;

		if(args.length == 3){
			location = args[2];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[2], args[3], args[4]);
		}
        
		var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
		Block = location.getBlock();

		if(typeof itemID==='number' && (itemID%1)===0){
			for each (Entity in Block.getWorld().getNearbyEntities(Block.getLocation(), 2, 2, 2)){
				if(typeof Entity.setItem == 'function'){
					Entity.setItem(new ItemStack(itemID, 1, 0, itemData));
				}
			}
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someBlock = Material.valueOf(itemID.toUpperCase());
			for each (Entity in Block.getWorld().getNearbyEntities(Block.getLocation(), 2, 2, 2)){
				if(typeof Entity.setItem == 'function'){
					Entity.setItem(new ItemStack(someBlock, 1, 0, itemData));
				}
			}
		}

	}else {
		throw new Error(
			'Invalid parameters. Need [Item<string or number>, Location<location or number number number>] or [Item<string or number>, ItemData<number>, Location<location or number number number>]');
	}
	return null;
}