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
var plType = Java.type("org.bukkit.entity.Player")

validation = {
	overloads: [
		[],
		[{"name": "player", "type": plType.class}],
		[{"name": "reason", "type": "string"}],
		[{"name": "player", "type": plType.class}, {"name": "reason", "type": "string"}]
	]
}

 function KICK(args) {
    switch (overload) {
    case 0:
    	if(player === null){
            throw new Error("Too few arguments! You should enter at least on argument if you use KICK executor from console.")
        }
        player.kickPlayer(ChatColor.translateAlternateColorCodes('&', "&c[TR] You've been kicked from the server."))
        break

    case 1:
    	args[0].kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), "&c[TR] You've been kicked from the server."))
    	break

    case 2:
    	if(player === null){
            throw new Error("player should not be null")
        }
    	player.kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), args[0]))
    	break

    case 3:
    	args[0].kickPlayer(ChatColor.translateAlternateColorCodes(Char('&'), args[1]))
    	break
    }
}
