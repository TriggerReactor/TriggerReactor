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
validation = {
	"overloads": [
		[{"name": "minimum", "type": "number"}],
		[{"name": "minimum", "type": "number"}, {"name": "maximum", "type": "number"}]
	]
}

function random(args){
	if(args.length == 1){
		if(typeof args[0] !== "number")
			throw new Error("Invalid parameter! [Number]");
			
		return Math.floor(Math.random() * args[0]);
	}else if(args.length == 2){
		if(typeof args[0] !== "number"
			|| typeof args[1] !== "number")
			throw new Error("Invalid parameter! [Number, Number]");
			
		return Math.floor(Math.random() * (args[1] - args[0])) + args[0];
	}else{
		throw new Error("Invalid parameter! [Number] or [Number, Number]");
	}
}