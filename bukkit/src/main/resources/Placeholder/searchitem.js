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
var Integer = Java.type('java.lang.Integer');
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
    if(player == null)
        throw new Error('Unexpected error found! player cannot be null.');


    var contents = player.getInventory().getContents();
    var maximum = (contents.length);
    switch (overload) {
        case 0:
        case 1:
        case 2:
            if (!(overload === 0)) {
                var check = (overload === 1) ? args[1].substring(args[1].length() - 1) : "none";
                if (check.equals("none")) {
                    if (args[1] > args[2]) {
                        start = args[2];
                        end = args[1];
                    } else if (args[1] < args[2]) {
                        start = args[1];
                        end = args[2];
                    } else {
                        var same = true;
                        var pos = args[1];
                    }
                    if (same) {
                        if (args[0].isSimilar(player.getInventory().getItem(pos)))
                            return pos;
                        else
                            return -1;
                    } else {
                        if (!bothHaveValidInt(start, end, maximum))
                            throw new Error('Unexpected error found! both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                        for (var i = start; i < end; i++) {
                            if (args[0].isSimilar(contents[i]))
                                return i.intValue();
                        }
                        return -1;
                    }
                } else {
                    var lastChar = check;
                    var check2 = (lastChar.equalsIgnoreCase("s") || lastChar.equalsIgnoreCase("e")) ? lastChar : "invalid";
                    if (check2.equals("invalid") || !args[1].substring(0, args[1].length() - 1).matches("[0-9]+"))
                        throw new Error('The provided FromTo string was invalid! You have to follow the rule: \"[int]s\" or \"[int]e\"')

                    if(check2.equalsIgnoreCase("s")) {
                        start = new Integer(args[1].substring(0, args[1].length() - 1));
                        end = player.getInventory().getContents().length;
                    }else if(check2.equalsIgnoreCase("e")){
                        start = 0;
                        end = new Integer(args[1].substring(0, args[1].length() - 1));
                    }else{
                        return check2;
                    }
                    var contents = player.getInventory().getContents();
                    if (!bothHaveValidInt(start, end, maximum))
                        throw new Error('Unexpected error found! Both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                    for (var i = start; i < end; i++) {
                        if (args[0].isSimilar(contents[i]))
                            return i.intValue();
                    }
                    return -1;
                }
            } else{
                for(var i = 0; i < contents.length; i++){
                    if (args[0].isSimilar(contents[i]))
                        return i.intValue();
                }
                return -1;
            }
        case 3:
        case 4:
        case 5:
            if (!(overload === 3)) {
                var check = (overload === 4) ? args[1].substring(args[0].length() - 1) : "none";
                if (!check.equals("none")) {
                    if (args[1] > args[2]) {
                        start = args[2];
                        end = args[1];
                    } else if (args[1] < args[2]) {
                        start = args[1];
                        end = args[2];
                    } else {
                        var same = true;
                        var pos = args[1];
                    }
                    if (same) {
                        var posItem = player.getInventory().getItem(pos);
                        if(posItem == null)
                            return -1;

                        if (args[0].equals(posItem.getType()))
                            return pos;
                        else
                            return -1;
                    } else {
                        if (!bothHaveValidInt(start, end, maximum))
                            throw new Error('Unexpected error found! Both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                        for (var i = start; i < end; i++) {
                            if(contents[i] == null)
                                continue;

                            if (args[0].equals(contents[i].getType()))
                                return i.intValue();
                        }
                        return -1;
                    }
                } else {
                    var lastChar = args[1].substring(args[0].length() - 1);
                    var check2 = (lastChar.equalsIgnoreCase("s") || lastChar.equalsIgnoreCase("e")) ? lastChar : "invalid";
                    if (check2.equals("invalid") || !args[1].substring(0, args[1].length() - 1).matches("[0-9]+"))
                        throw new Error('The provided FromTo string was invalid! You have to follow the rule: \"[int]s\" or \"[int]e\"')

                    if(check2.equalsIgnoreCase("s")) {
                        start = new Integer(args[1].substring(0, args[1].length() - 1));
                        end = player.getInventory().getContents().length;
                    }else {
                        start = 0;
                        end = new Integer(args[1].substring(0, args[1].length() - 1));
                    }
                    var contents = player.getInventory().getContents();
                    if (!bothHaveValidInt(start, end, maximum))
                        throw new Error('Unexpected error found! Both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                    for (var i = start; i < end; i++) {
                        if(contents[i] == null)
                            continue;

                        if (args[0].equals(contents[i].getType()))
                            return i.intValue();
                    }
                    return -1;
                }
            } else{
                for(var i = 0; i < contents.length; i++){
                    if(contents[i] == null)
                        continue;

                    if (args[0].equals(contents[i].getType()))
                        return i.intValue();
                }
                return -1;
            }
        case 6:
        case 7:
        case 8:
            if (!(overload === 6)) {
                var check = (overload === 7) ? args[1].substring(args[0].length() - 1) : "none";
                if (!check.equals("none")) {
                    if (args[1] > args[2]) {
                        start = args[2];
                        end = args[1];
                    } else if (args[1] < args[2]) {
                        start = args[1];
                        end = args[2];
                    } else {
                        var same = true;
                        var pos = args[1];
                    }
                    if (same) {
                        var posItem = player.getInventory().getItem(pos);
                        if(posItem == null)
                            return -1;

                        if (args[0].equalsIgnoreCase(posItem.getType().name()))
                            return pos;
                        else
                            return -1;
                    } else {
                        if (!bothHaveValidInt(start, end, maximum))
                            throw new Error('Unexpected error found! Both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                        for (var i = start; i < end; i++) {
                            if(contents[i] == null)
                                continue;

                            if (args[0].equals(contents[i].getType()))
                                return i.intValue();
                        }
                        return -1;
                    }
                } else {
                    var lastChar = args[1].substring(args[0].length() - 1);
                    var check2 = (lastChar.equalsIgnoreCase("s") || lastChar.equalsIgnoreCase("e")) ? lastChar : "invalid";
                    if (check2.equals("invalid") || !args[1].substring(0, args[1].length() - 1).matches("[0-9]+"))
                        throw new Error('The provided FromTo string was invalid! You have to follow the rule: \"[int]s\" or \"[int]e\"')

                    if(check2.equalsIgnoreCase("s")) {
                        start = new Integer(args[1].substring(0, args[1].length() - 1));
                        end = player.getInventory().getContents().length;
                    }else {
                        start = 0;
                        end = new Integer(args[1].substring(0, args[1].length() - 1));
                    }
                    var contents = player.getInventory().getContents();
                    if (!bothHaveValidInt(start, end, maximum))
                        throw new Error('Unexpected error found! Both start index and end index cannot be larger than inventory\'s size: ' + contents.length);

                    for (var i = start; i < end; i++) {
                        if(contents[i] == null)
                            continue;

                        if (args[0].equalsIgnoreCase(contents[i].getType().name()))
                            return i.intValue();
                    }
                    return -1;
                }
            } else{
                for(var i = 0; i < contents.length; i++){
                    if(contents[i] == null)
                        continue;

                    if (args[0].equalsIgnoreCase(contents[i].getType().name()))
                        return i.intValue();
                }
                return -1;
            }

    }
}

function bothHaveValidInt(value1, value2, maximum){
    return (value1 <= maximum && value2 <= value2);
}