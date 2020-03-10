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
var itemStackType = Java.type('org.bukkit.inventory.ItemStack');
var Math = Java.type('java.util.Math')
validation = {
	"overloads": [
		[{"name":"item", "type": itemStackType.class}]
		[{"name":"item", "type":itemStackType.class}, {"name":"stackable","type":"boolean"}, {"name":"dropIfFull","type":"boolean"}]
	]
}
function GIVE(args){
	if(player == null)
		return;

	var inv = player.getInventory();
	if(overload === 0){
		if(inv.firstEmpty() === -1)
			throw new Error("Player has no empty slot.");


		player.getInventory().addItem(args[0]);
		return;
	}else if(overload === 1){
		var item = args[0];
		var stackable = args[1];
		var dropable = args[2];
		if(!stackable && !dropable){
			if(inv.firstEmpty() === -1)
				throw new Error("Player has no empty slot.");

			inv.addItem(item);
			return;
		}else{
			var contents = inv.getContents();
			var count = 0;
			for(var i = 0; i < contents.length; i++){
				if(contents[i] == null)
					count++;
			}
			var setAmount = Math.floor((item.getAmount() / item.getMaxStackSize()));
			var nonMax = item.getAmount() - (item.getMaxStackSize * setAmount)
			if(!stackable && dropable){
				if(count <= setAmount){
					var vItemDrop = item.clone().setAmount(item.getAmount() - (count * item.getMaxStackSize()));
					var vItemAdd = item.clone().setAmount(item.getMaxStackSize());
					for(var i = 0; i < count; i++){
						inv.addItem(vItemAdd);
					}
					player.getWorld().dropItem(player.getLocation(), vItemDrop);
					return;
				}else if(count > setAmount){
					var vItemAdd = item.clone().setAmount(item.getMaxStackSize());
					var vItemAdd2 = item.clone().setAmount(item.getAmount() - (count * item.getMaxStackSize()));
					for(var i = 0; i < count; i++){
						inv.addItem(vItemAdd);
					}
					inv.addItem(vItemAdd2);
					return;
				}
			}else if(stackable && !dropable){
				var itemMap = allIgnoreAmount(inv, item);
				var keyset = itemMap.keySet();
				var keysetIterator = keyset.iterator();
				while (keysetIterator.hasNext()){
					//something
				}
			}else {

			}
		}
	}
}
function allIgnoreAmount(inventory, item){
	var contents = inventory.getContents();
	var map = inventory.all().clear();
	for(var k = 0; k < contents.length; k++){
		if(contents[k] !== null && contents[k].isSimilar(item))
			map.put(k, contents[i]);
	}
	return map;
}