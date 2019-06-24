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
function GIVE(args){
	if(args.length == 1){
		var QueryOperationTypes = Java.type('org.spongepowered.api.item.inventory.query.QueryOperationTypes');
		var MainPlayerInventory = Java.type('org.spongepowered.api.item.inventory.entity.MainPlayerInventory');
		var main = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(MainPlayerInventory.class))
	
		var result = main.offer(args[0]);
		
		if(!result.getRejectedItems().isEmpty()){
			throw new Error("Player has no empty slot.");
		}
	}else{
		throw new Error("Invalid parameters. Need [ItemStack]")
	}
}