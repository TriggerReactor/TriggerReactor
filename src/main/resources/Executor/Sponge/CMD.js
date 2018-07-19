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
	for(var i = 0; i < args.length; i++){
		var split = args[i].split(" ");
	
		var mapping = Sponge.getCommandManager().get(split[0]).orElse(null);
		if(mapping == null)
			continue;
		
		if(mapping.getCallable().testPermission(player)){
			var merged = "";
			for(var i = 1; i < split.length; i++)
				merged += split[i] + " ";
				
			mapping.getCallable().process(player, merged);
		} else {
			player.sendMessage(TextUtil.colorStringToText("&cYou do not have enough permission."));
		}
	}

	return null;
}