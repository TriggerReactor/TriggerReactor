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
	if(args.length === 1){
		if (typeof args[0] !== "number") {
			throw new Error("Invalid number for seconds to burn: " + args[0])
		}
		
		if (args[0] < 0) {
			throw new Error("The number of seconds to burn should be positive")
		}
		
		player.setFireTicks(Math.round(args[0] * 20));
	} else if(args.length === 2) { 
		var entity = args[0];

		if (entity === null) {
			throw new Error("player to burn should not be null")
		}
		
		if(typeof entity === "string"){
			entity = Bukkit.getPlayer(entity);
			
			if (entity === null) {
				throw new Error("player to burn does not exist")
			}
		}
		else if (!(entity instanceof Java.type("org.bukkit.entity.Entity"))) {
			throw new Error("invalid entity to burn: " + entity)
		}
		
		if (typeof args[1] !== "number") {
			throw new Error("The number of seconds to burn should be a number")
		}
		
		if (args[1] < 0) {
			throw new Error("The number of seconds to burn should be positive")
		}

		entity.setFireTicks(Math.round(args[1] * 20));
	} else {
		throw new Error(
			'Invalid number of parameters. Need [Number] or [Entity<entity or string>, Number]');
	}
	return null;
}