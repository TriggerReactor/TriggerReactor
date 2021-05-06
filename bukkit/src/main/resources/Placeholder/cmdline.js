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
    var splitMsg; //target message array
    var message = event.getMessage().substring(1); //first character is slash

    if (overload === 0) {
        return message;
    } else if (overload === 1) {
        splitMsg = message.split(" ");
        return merge(splitMsg, args[0], splitMsg.length - 1);
    } else if (overload === 2) {
        splitMsg = message.split(" ");
        if (args[0] > args[1])
            return merge(splitMsg, args[1], args[0]); //if toIndex is smaller than fromIndex, reversed result can be returned.

        //throw new Error("fromIndex cannot be larger than toIndex!");


        return merge(splitMsg, args[0], args[1]);
    }
}

function merge(targetArr, indexFrom, indexTo) {
    indexFrom = Math.max(0, indexFrom)
    indexTo = Math.min(targetArr.length - 1, indexTo)

    if (indexTo - indexFrom < 1) {
        if (indexFrom < targetArr.length)
            return targetArr[indexFrom];
        else
            return null;
    }

    var temp = targetArr[indexFrom];
    for (var i = indexFrom + 1; i <= indexTo; i++) {
        temp += " " + targetArr[i];
    }

    return temp;
}