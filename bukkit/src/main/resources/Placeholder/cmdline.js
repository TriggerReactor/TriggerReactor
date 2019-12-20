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
validation = {
    "overloads": [
        [], // whole line
        [{"type": "int", "minimum": 0, "name": "fromIndex"}], //initial index to end
        [{"type": "int", "minimum": 0, "name": "fromIndex"},
            {"type": "int", "minimum": 0, "name": "toIndex"}] //initial to final
    ]
}

function cmdline(args) {
    var message = event.getMessage().substring(1); //first character is slash

    if (overload == 0) {
        return message;
    } else if (overload == 1) {
        var split = message.split(" ");
        return merge(split, args[0], split.length - 1);
    } else if (overload == 2) {
        var split = message.split(" ");
        if (args[0] > args[1])
            throw new Error("fromIndex cannot be larger than toIndex!");

        return merge(split, args[0], args[1]);
    }
}

function merge(split, indexFrom, indexTo) {
    indexFrom = Math.max(0, indexFrom)
    indexTo = Math.min(split.length - 1, indexTo)

    if (indexTo - indexFrom < 1) {
        if (indexFrom < split.length)
            return split[indexFrom];
        else
            return null;
    }

    var temp = split[indexFrom];
    for (var i = indexFrom + 1; i <= indexTo; i++) {
        temp += " " + split[i];
    }

    return temp;
}