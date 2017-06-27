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
 function FALLINGBLOCK(args) {
	if (args.length == 2 || args.length == 4) {
		var blockID = args[0];
		var location;

		if(args.length == 2){
			location = args[1];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[1], args[2], args[3]);
		}

		Block = location.getBlock();

		if(typeof blockID==='number' && (blockID%1)===0){
			location.getWorld().spawnFallingBlock(location, blockID, 0);
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someBlock = Material.valueOf(blockID.toUpperCase());
			if(someBlock.isBlock()){
				location.getWorld().spawnFallingBlock(location, someBlock, 0);
			}
		}

	}else if(args.length == 3 || args.length == 5){ 
		var blockID = args[0];
		var blockData = args[1];
		var location;

		if(args.length == 3){
			location = args[2];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[2], args[3], args[4]);
		}

		Block = location.getBlock();

		if(typeof blockID==='number' && (blockID%1)===0){
			location.getWorld().spawnFallingBlock(location, blockID, blockData);
		}else{
			var Material = Java.type('org.bukkit.Material');
			var someBlock = Material.valueOf(blockID.toUpperCase());
			if(someBlock.isBlock()){
				location.getWorld().spawnFallingBlock(location, someBlock, blockData);
			}
		}

	}else {
		throw new Error(
			'Invalid parameters. Need [Block<string or number>, Location<location or number number number>] or [Block<string or number>, BlockData<number>, Location<location or number number number>]');
	}
	return null;
}