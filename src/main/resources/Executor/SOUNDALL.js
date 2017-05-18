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
function SOUNDALL(args){
	if(args.length == 4){
		var location = args[0];
		var sound = args[1];
		var volume = args[2];
		var pitch = args[3];
		
		player.getWorld().playSound(location, sound, volume, pitch);
	} else {
		throw new Error("Invalid parameters. Need [Location, Sound, Number, Number]")
	}
	return null;
}