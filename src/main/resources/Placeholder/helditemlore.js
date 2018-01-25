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
function helditemlore(args){
	if(player == null)
		return null;
		
	if(player.getItemInHand() == null)
		return "";
		
	if(args.length < 1)
		return "";
		
	if(typeof args[0] !== "number")
		throw new Error("Invalid parameter! helditemlore accepts 'number' as paramter.");
		
	var itemMeta = player.getItemInHand().getItemMeta();
	if(itemMeta == null)
		return "";
	
	var lores = itemMeta.getLore();
	if(lores == null)
		return "";
		
	var index = args[0] | 0;
	if(index < 0 || lores.size() <= index)
		return "";
		
	return lores[index];
}