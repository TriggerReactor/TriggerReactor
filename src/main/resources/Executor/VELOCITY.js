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
	
	args.forEach(function (value, i) {
		if (typeof value == "string") {args[i] = parseFloat(value);}
	});
	
	var Vector = Java.type('org.bukkit.util.Vector');
	player.setVelocity(new Vector(args[0].doubleValue(), args[1].doubleValue(), args[2].doubleValue()));

	return null;
}
