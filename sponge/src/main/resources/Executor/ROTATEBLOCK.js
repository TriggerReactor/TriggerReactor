/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
 function ROTATEBLOCK(args) {
	var Direction = Java.type('org.spongepowered.api.util.Direction');
	var Keys = Java.type('org.spongepowered.api.data.key.Keys');
	 
	if(block != null && args.length == 1){
		var dir = Direction.valueOf(args[0]);

		//block is Location in sponge
		block.set(Keys.DIRECTION, dir);
	} else if(args.length == 2 || args.length == 4){
		var face = Direction.valueOf(args[0]);
		var location;
		
		if(args.length == 4){
			location = new Location(player.getWorld(), args[1], args[2], args[3]);
		}else{
			location = args[1];
		}
		
		var dir = Direction.valueOf(args[0]);

		location.set(Keys.DIRECTION, dir);
	} else {
		throw new Error(
			'Invalid parameters. Need [Direction<string>] or [Direction<string>, Location<location or number number number>]');
	}
	return null;
}