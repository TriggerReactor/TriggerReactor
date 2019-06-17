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
function MODIFYHELDITEM(args){
	if(player === null)
		return null;
		
	if(args.length < 2)
		throw new Error("Invalid parameters. Need [String, Depends on type]");
		
	if(typeof args[0] !== "string")
		throw new Error("Invalid parameters. First parameter wasn't a String");
		
	var type = args[0].toUpperCase();
	var helditem = player.getItemInHand();
		
	if(type.equals("TITLE")){
		if(helditem == null || helditem.getType().name() == "AIR")
			return null;
			
		var meta = helditem.getItemMeta();
		meta.setDisplayName(args[1]);
		helditem.setItemMeta(meta);
	}else if(type.equals("LORE")){
		if(args.length < 3)
			throw new Error("Invalid parameters. Need [String, String, Depends on action]");
		
		if(typeof args[0] !== "string")
			throw new Error("Invalid parameters. Second parameter wasn't a String");
		var action = args[1].toUpperCase();
		
		if(helditem == null || helditem.getType().name() == "AIR")
			return null;
			
		var meta = helditem.getItemMeta();
		var lore = meta.getLore();
		if(lore == null){
			var ArrayList = Java.type('java.util.ArrayList');
			lore = new ArrayList();
		}
		
		if(action.equals("ADD")){
			var value = args[2].toString();
			
			if(args.length > 3){
				var index = args[3];
				if(typeof index !== "number")
					throw new Error("index should be a number!");
				
				if(index > lore.size() - 1)
					lore.add(value);
				else
					lore.add(Math.max(0, Math.min(lore.size() - 1, index)), value);
			}else{
				lore.add(value);
			}
		}else if(action.equals("SET")){
			if(args.length < 4)
				throw new Error("Invalid parameters. Need [String, String, Any, Number]");
			
			while((lore.size() - 1) < args[3])
				lore.add("");
			
			lore.set(args[3], args[2]);
		}else if(action.equals("REMOVE")){
			var index = args[2];
			if(typeof index !== "number")
				throw new Error("index should be a number!");
				
			if(0 <= index && index < lore.size()){
				lore.remove(index);
			}
		}else{
			throw new Error("Unknown MODIFYHELDITEM LORE action "+action);
		}
		
		meta.setLore(lore);
		helditem.setItemMeta(meta);
	}else{
		throw new Error("Unknown MODIFYHELDITEM type "+type);
	}
	
	player.setItemInHand(helditem);
		
	return null;
}