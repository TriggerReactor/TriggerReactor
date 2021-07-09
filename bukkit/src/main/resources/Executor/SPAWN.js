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
var EntityType = Java.type("org.bukkit.entity.EntityType");

function SPAWN(args){
	var world = player.getWorld();
	
	if(args.length == 1){
		var entity = EntityType.valueOf(args[0]);
		
		world.spawnEntity(player.getLocation(), entity);
	}else if(args.length == 2){
		var loc = args[0];
		var entity = EntityType.valueOf(args[1]);
		
		world.spawnEntity(loc, entity);
	}else{
		throw new Error("Invalid parameters. Need [String] or [Location, String]")
	}
}