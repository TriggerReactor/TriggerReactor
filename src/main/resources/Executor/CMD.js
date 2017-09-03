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
function CMD(args){
	if(args.length == 2 && args[1].equals(true)){
		var preCommandSize = args[0].split(" ").length;
		var split = message.split(" ");
		
		var merged = "";
		for(var i = 1; i < split.length; i++)
			merged += split[i] + " ";
		
		Bukkit.dispatchCommand(player, args[0]+" "+merged);
	} else {
		Bukkit.dispatchCommand(player, args[0]);
	}

    return null;
}