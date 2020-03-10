/*******************************************************************************
 *     Copyright (C) 2020 Dr_Romantic(a.k.a. Pro_Snape)
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
var itemstackType = Java.type('org.bukkit.inventory.ItemStack');
var materialType = Java.type('org.bukkit.Material')
var Player = Java.type('org.bukkit.entity.Player')
validation = {
    "overloads": [
        [{"name":"item","type": itemstackType.class}],
        [{"name":"item","type": itemstackType.class}, {"name":"player", "type":Player.class}],
        [{"name":"MaterialType","type":materialType.class}],
        [{"name":"MaterialType","type":materialType.class}, {"name":"player", "type":Player.class}],
        [{"name":"StringType", "type":"string"}],
        [{"name":"StringType", "type":"string"}, {"name":"player", "type":Player.class}]
    ]

}

function countitem(args){
    var count = 0;
    if(overload === 0 || overload === 1){
        var item = args[0];
        if(overload === 1)
            player = args[1];

        if(player == null)
            throw new Error('Unexpected error found! player cannot be null.')

        var inv = player.getInventory();
        if(!inv.contains(item)){
            return count;
        }else {
            var contents = inv.getContents();
            for(i = 0; i < contents.length; i++){
                if(contents[i] == null)
                    continue;

                if(item.isSimilar(contents[i])){
                    count = count + contents[i].getAmount();
                }
            }
            return count.intValue();
        }
    }

    if(overload === 2 || overload === 3){
        var type = args[0];
        if(overload === 3)
            player = args[1];

        if(player == null)
            throw new Error('Unexpected error found! player cannot be null.')

        var inv = player.getInventory();
        if(!inv.contains(type)){
            return count;
        }else {
            var contents = inv.getContents();
            for(i = 0; i < contents.length; i++){
                if(contents[i] == null)
                    continue;

                if(contents[i].getType().equals(type)){
                    count = count + contents[i].getAmount();
                }
            }
            return count.intValue();
        }

    }
    if(overload === 4 || overload === 5){
        var typeString = args[0];
        if(overload === 5)
            player = args[1];

        if(player == null)
            throw new Error('Unexpected error found! player cannot be null.')

        if(materialType.valueOf(typeString.toUpperCase()) == null)
            throw new Error('Unexpected error found! The type id does not exist.')
        else
            var typeByString = materialType.valueOf(typeString.toUpperCase())

        var inv = player.getInventory();

        if(!inv.contains(typeByString)){
            return count;
        }else {
            var contents = inv.getContents();
            for(i = 0; i < contents.length; i++){
                if(contents[i] == null)
                    continue;

                if(contents[i].getType().equals(typeByString)){
                    count = count + contents[i].getAmount();
                }
            }
            return count.intValue();
        }

    }
}