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
 function SETBLOCK(args) {
	if (args.length == 2 || args.length == 4) {
		var blockID = args[0];
		var location;

		if(args.length == 2){
			location = args[1];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[1], args[2], args[3]);
		}

		var type = ReflectionUtil.getField(BlockTypes.class, null, blockID.toUpperCase());
		
		location.setBlockType(type);

	} else {
		throw new Error(
			'Invalid parameters. Need [Block<string>, Location<location or number number number>]');
	}
	return null;
}