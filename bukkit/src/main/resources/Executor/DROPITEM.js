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
 var ItemStack = Java.type('org.bukkit.inventory.ItemStack')
 var Enchantment = Java.type('org.bukkit.enchantments.Enchantment')
 var Location = Java.type('org.bukkit.Location')
 var Material = Java.type('org.bukkit.Material')
 var NamespacedKey = Java.type('org.bukkit.NamespacedKey')

 function DROPITEM(args) {
    var item;
    var location;

    if (args.length == 2){
		item = args[0];
		location = args[1];
    } else if (args.length == 4) {
        location = args[3];
    } else if (args.length == 6) {
		var world = player.getWorld();
        location = new Location(world, args[3], args[4], args[5]);
    } else {
        throw new Error(
            'Invalid parameters. Need [Location<location or number number number>]');
    }

    if(args.length == 4 || args.length == 6){
		var itemID = args[0];
		var amount = args[1];

		if(typeof itemID==='number' && (itemID%1)===0){
			item = new ItemStack(itemID, amount);
		}else{
			var someItem = Material.valueOf(itemID.toUpperCase());
			item = new ItemStack(someItem, amount);
		}

		var enchan = args[2];
		if(enchan.toUpperCase() !== 'NONE'){
			var encharg = enchan.split(',');
			for each (en in encharg){
				var ench = Enchantment.getByName(en.split(':')[0].toUpperCase());
				var level = parseInt(en.split(':')[1]);

				if(!ench)
					throw Error(en.split(':')[0]+" is not a valid Enchantment.");

				item.addUnsafeEnchantment(ench, level);
			}
		}
    }

    location.getWorld().dropItem(location, item);

	return null;
}