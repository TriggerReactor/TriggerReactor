/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape, wysohn
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
       [{"name": "slot", "type": "int", "minimum": 0, "maximum": 35}]
    ]
}
var QueryOperationTypes = Java.type('org.spongepowered.api.item.inventory.query.QueryOperationTypes')
var GridInventory = Java.type('org.spongepowered.api.item.inventory.type.GridInventory')
var ItemTypes = Java.type('org.spongepowered.api.item.ItemTypes')
var ItemStack = Java.type('org.spongepowered.api.item.inventory.ItemStack')
function playerinv(args){
    if(player == null)
        return null;

    if(overload == 1){
        var carriedInv = player.getInventory();
        var grids = carriedInv.query(QueryOperationTypes.INVENTORY_TYPE.of(GridInventory.class));

        var y = args[0] / 9;
        var x = args[0] % 9;

        var item = grids.peek(x, y).orElse(null);
        if(item == null){
            item = ItemStack.builder().itemType(ItemTypes.AIR).build();
        }

        return item;
    }
}
