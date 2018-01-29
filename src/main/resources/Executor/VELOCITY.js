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
function VELOCITY(args){
	if(player === null)
		return null;
	
	if(args.length != 3)
		throw new Error("Invalid parameters! [Number, Number, Number]");
		
	if(typeof args[0] !== "number"
		|| typeof args[1] !== "number"
		|| typeof args[2] !== "number")
		throw new Error("Invalid parameters! [Number, Number, Number]");
	
	var Vector = Java.type('org.bukkit.util.Vector');
	player.setVelocity(new Vector(args[0], args[1], args[2]));

	return null;
}