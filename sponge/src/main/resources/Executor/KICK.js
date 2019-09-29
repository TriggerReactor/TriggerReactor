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
 function KICK(args) {
    var plType = Java.type("org.spongepowered.api.entity.living.player.Player")
    var String = Java.type('java.lang.String')
    var Text = Java.type('org.spongepowered.api.text.Text')
    if(args.length === 0){
        if(player === null){
            throw new Error("Too few arguments! You should enter at least one argument if you use KICK executor from console.")
        }else {
            player.kick(TextUtil.colorStringToText("&c[TR] You've been kicked from the server."));
            return null;
        }
    }else if(args.length === 1) {
        var undefinedArgument = args[0]
        if(undefinedArgument === null)
            throw new Error("Unexpected Error: parameter does not match - player: null")

        if(args[0] instanceof plType) {
            var definedToPlayer = undefinedArgument;
            definedToPlayer.kick(TextUtil.colorStringToText("&c[TR] You've been kicked from the server."));
            return null;
        } else if(!(undefinedArgument instanceof plType)){
            if(msg instanceof Text)
                var msg = undefinedArgument;
            else
                var msg = TextUtil.colorStringToText(String.valueOf(undefinedArgument))

            player.kick(msg);
            return null;
        }else {
            throw new Error("Found unexpected type of argument: "+ undefinedArgument);
        }
    }else if(args.length === 2) {
        var pl = args[0]
        var msg = args[1]
        if(!(pl instanceof plType)){
            throw new Error("Found unexpected type of argument(s) - player: "+pl+" | msg: "+ str)
        }else {
            if(msg instanceof Text)
                var msg = undefinedArgument;
            else
                var msg = TextUtil.colorStringToText(String.valueOf(undefinedArgument))

            pl.kick(msg);
            return null;
        }
    }else if(args.length > 2){
       throw new Error("Too many arguments! KICK Executor accepts up to two arguments.")
    }
}