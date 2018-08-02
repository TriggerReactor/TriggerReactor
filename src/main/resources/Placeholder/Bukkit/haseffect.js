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
function haseffect(args){
	if(player == null)
		return null;

	if(args.length != 1 || typeof args[0] !== "string")
		throw new Error("Invalid parameter! [String]");
		
	var PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');
	var type = PotionEffectType.getByName(args[0]);
	if(type == null)
		throw new Error(args[0]+" is not a valid PotionEffectType!");
		
	return player.hasPotionEffect(type);
}