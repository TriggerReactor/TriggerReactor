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
var itemStackType = Java.type('org.bukkit.inventory.ItemStack')
validation = {
    "overloads": [
        [{"name":"name", "type": "string"}, {"name": "item", "type": itemStackType.class}]
    ]

}
function SETITEMNAME(args){
    var item = args[1];
    var name = ChatColor.translateAlternateColorCodes(Char('&'), args[0]);
    if(item.getType().name().toLowerCase().equals("air") || item == null){
        return null;
    } else{
        var im = item.getItemMeta();
        im.setDisplayName(name);
        item.setItemMeta(im);
        return null;
    }
}