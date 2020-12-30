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
    var cmd = event.getCommand();
    var args = event.getArguments().split(" ");

    var temp = cmd;
    if (overload == 0) {
        // do nothing
    } else if (overload == 1) {
        for(int i = args[0]; i < args.length; i++){
            temp += " " + args[i];
        }
    } else if (overload == 2) {
        var split = message.split(" ");
        if (args[0] > args[1])
            throw new Error("fromIndex cannot be larger than toIndex!");

        for(int i = args[0]; i <= args[1]; i++){
            temp += " " + args[i];
        }
    } else {
        throw new Error("Invalid argument size. Usage) $cmdline:[from (inclusive)]:[to (inclusive)]");
    }

    return temp;
}