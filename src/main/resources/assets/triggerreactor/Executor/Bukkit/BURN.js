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
function BURN(args){
	if(args.length == 1){
		var seconds = args[0];
		
		player.setFireTicks(seconds * 20);
	}else if(args.length == 2){ 
		var entity = args[0];
		var seconds = args[1];

		if(typeof entity == "string"){
			entity = Bukkit.getPlayer(entity);
		}

		entity.setFireTicks(seconds * 20);
	}else {
		throw new Error(
			'Invalid parameters. Need [Number] or [Entity<entity or string>, Number]');
	}
	return null;
}