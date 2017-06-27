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
 function ITEMFRAMEROTATE(args) {
	if (args.length == 2 || args.length == 4) {
		var Rotation = Java.type('org.bukkit.Rotation');
		var location;

		if(args.length == 2){
			location = args[1];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[1], args[2], args[3]);
		}

		Block = location.getBlock();

		for each (Entity in Block.getWorld().getNearbyEntities(Block.getLocation(), 2, 2, 2)){
			if(typeof Entity.setItem == 'function'){
				Entity.setRotation(Rotation.valueOf(args[0].toUpperCase()));
			}
		}


	}else {
		throw new Error(
			'Invalid parameters. Need [Rotation<string>, Location<location or number number number>]');
	}
	return null;
}