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
var itemStackType = Java.type("org.bukkit.inventory.ItemStack")
validation = {
    "overloads": [
        [{"name": "type", "type": "string"}, {"name": "item", "type": itemStackType.class}]
    ]

}

function SETTYPE(args) {
    if (overload === 0) {
        var typeString = args[0].toUpperCase()
        var item = args[1]
        var Material = Java.type("org.bukkit.Material")
        if (item == null || item.getType() === Material.AIR)
            throw new Error("Error: item cannot be null or AIR.")

        var Materials = Material.values();
        var validType = false;
        for (var i = 0; i < Materials.length; i++) {
            if (Materials[i].name() === typeString)
                validType = true;
        }
        if (!validType)
            throw new Error("Error: the type provided is not valid type.")
        else
            item.setType(Material.valueOf(typeString))
    }
}