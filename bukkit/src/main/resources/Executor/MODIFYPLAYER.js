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
function MODIFYPLAYER(args){
	if(player === null)
		return null;
		
	if(args.length != 2)
		throw new Error("Invalid parameters. Need [String, Depends on type]");
		
	if(typeof args[0] !== "string")
		throw new Error("Invalid parameters. First parameter wasn't a String");
		
	var type = args[0].toUpperCase();
	var value = args[1];
		
	switch(type){
		case "HEALTH":
			player.setHealth(value);
			break;
		case "FOOD":
			player.setFoodLevel(value);
			break;
		case "SATURATION":
			player.setSaturation(value);
			break;
		case "EXP":
			player.setExp(value);
			break;
		case "WALKSPEED":
			player.setWalkSpeed(value);
			break;
		case "FLYSPEED":
			player.setFlySpeed(value);
			break;
		case "FLY":
			player.setAllowFlight(value);
			player.setFlying(value);
			break;
		case "GAMEMODE":
			try{
				var GameMode = Java.type('org.bukkit.GameMode')
				var mode = GameMode.valueOf(value.toUpperCase());
				player.setGameMode(mode);
			}catch(ex){
				throw new Error("Unknown GAEMMODE value "+value);
			}
			break;
		case "MAXHEALTH":
			player.setMaxHealth(value);
			break;
		default:
			throw new Error("Unknown MODIFYPLAYER type "+type);
	}
		
	return null;
}