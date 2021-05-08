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
function LIGHTNING(args){
	if(args.length !== 1 && args.length !== 4)
		throw new Error("Invalid parameters! [String, Number, Number, Number] or [Location]");

	if(args.length ===
		1){
	    var Location = Java.type('org.bukkit.Location')
	    if(!(args[0] instanceof Location))
	        throw new Error("Invalid parameters! [String, Number, Number, Number] or [Location]")

	    var loc = args[0]
	    var world = loc.getWorld()
	    world.strikeLightning(loc)
	}
	if(args.length === 4){
		if(typeof args[0] !== "string" || typeof args[1] !== "number" || typeof args[2] !== "number" || typeof args[3] !== "number")
	    	throw new Error("Invalid parameters! [String, Number, Number, Number] or [Location]");
		
	    var world = Bukkit.getWorld(args[0]);
	    if(world === null)
	    	throw new Error("Unknown world named "+args[0]);
	
	    var Location = Java.type('org.bukkit.Location');
	    world.strikeLightning(new Location(world, args[1], args[2], args[3]));
	}
}