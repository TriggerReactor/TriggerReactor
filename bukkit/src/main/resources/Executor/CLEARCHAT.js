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
function CLEARCHAT(args){
	if(player == null)
		return null;
	
	if(args.length < 1){	
		for(var i = 0; i < 30; i++)
			player.sendMessage("");
		
		return null;
	}
	if(args.length == 1){
	    var plType = Java.type("org.bukkit.entity.Player")
	    if(args[0] instanceof plType){
            pl = args[0];
            for(var i = 0; i < 30; i++)
                pl.sendMessage("");
        
            return null;
        	}else {
            	throw new Error("Found unexpected parameter - player: " + args[0])
        }
	if(args.length > 1)
		throw new Error("There's too many arguments. CLEARCHAT Executor accepts up to one argument.")
	}
    }
}
