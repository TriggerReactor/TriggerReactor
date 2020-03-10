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
var materialType = Java.type('org.bukkit.Material');
var start = 0;
var end = 0;

validation = {
    "overloads": [
        [{"name": "item", "type": itemstackType.class}],
        [{"name": "item", "type": itemstackType.class}, {"name": "FromOrTo", "type": "string"}],
        [{"name": "item", "type": itemstackType.class}, {"name": "startFrom", "type": "int"}, {
            "name": "endIn",
            "type": "int"
        }],
        [{"name": "MaterialType", "type": materialType.class}],
        [{"name": "MaterialType", "type": materialType.class}, {"name": "FromOrTo", "type": "string"}],
        [{"name": "MaterialType", "type": materialType.class}, {"name": "startFrom", "type": "int"}, {
            "name": "endIn",
            "type": "int"
        }],
        [{"name": "TypeString", "type": "string"}],
        [{"name": "TypeString", "type": "string"}, {"name": "FromOrTo", "type": "string"}],
        [{"name": "TypeString", "type": "string"}, {"name": "startFrom", "type": "int"}, {
            "name": "endIn",
            "type": "int"
        }]
    ]
}

function searchitem(args) {
    switch (overload) {
        case 0, 1, 2:
            if (!overload === 0)
                var check = (overload === 1) ? args[1].substring(args[0].length() - 1) : "none";
            if (!check.equals("none")) {
                start = args[1];
                end = args[2];
            } else {
                var lastChar = args[1].substring(args[0].length() - 1);
                var check2 = (lastChar.equalsIgnoreCase("s") || lastChar.equalsIgnoreCase("e")) ? lastChar : "invalid";
                if (check.equals("invalid"))
                    throw new Error('The provided FromTo string was invalid! you have to follow the rule: \"[int]s\" or \"[int]e\"')

                var int
            }
    }
}