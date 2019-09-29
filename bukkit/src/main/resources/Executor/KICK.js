/*******************************************************************************
 *     Copyright (C) 2019 wysohn
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
    var plType = Java.type("org.bukkit.entity.Player")
    if(args.length === 0){
        if(player === null){
            throw new Error("Too few arguments! You should enter at least on argument if you use KICK executor from console.")
        }else {
            player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&c[TR] You've been kicked from the server."));
            return null;
        }
    }else if(args.length === 1) {
        var undefinedArgument = args[0]
        if(undefinedArgument === null)
            throw new Error("Unexpected Error: parameter does not match - player: null")

        if(args[0] instanceof plType) {
            var definedToPlayer = undefinedArgument;
            definedToPlayer.kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), "&c[TR] You've been kicked from the server."));
            return null;
        } else if(typeof undefinedArgument === "string"){
            var msg = undefinedArgument;
            player.kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), msg));
            return null;
        }else {
            throw new Error("Found unexpected type of argument: "+ undefinedArgument);
        }
    }else if(args.length === 2) {
        var pl = args[0]
        var str = args[1]
        if(!(pl instanceof plType) || !(typeof str === "string")){
            throw new Error("Found unexpected type of argument(s) - player: "+pl+" | msg: "+ str)
        }else {
            pl.kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), str))
            return null;
        }
    }else if(args.length > 2){
       throw new Error("Too many arguments! KICK Executor accepts up to two arguments.")
    }

}