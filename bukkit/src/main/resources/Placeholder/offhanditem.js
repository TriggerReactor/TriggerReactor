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
        []
    ]
}
function offhanditem(args){
    if(player == null)
        return null;

    var item = player.getInventory().getItemInOffHand();
    if(item == null){
      var ItemStack = Java.type('org.bukkit.inventory.ItemStack')
      var Material = Java.type('org.bukkit.Material')
      return ItemStack(Material.AIR);
    }else {
      return item;
    }
}