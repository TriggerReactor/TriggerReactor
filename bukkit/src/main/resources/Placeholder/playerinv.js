/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
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
       [{"name": "slot", "type": "int"}]
    ]
}
function playerinv(args){
    if(player == null)
        return null;

    if(args[0] < 0 || args[0] >= player.getInventory().getSize())
        throw new Error('Unexpected token: slot number should be at least 0, up to 35.');
    else
        var item = player.getInventory().getItem(args[0]);

    if(item == null){
        var ItemStack = Java.type('org.bukkit.inventory.ItemStack')
        var Material = Java.type('org.bukkit.Material')
        return new ItemStack(Material.AIR);
    }else {
        return item;
    }
}