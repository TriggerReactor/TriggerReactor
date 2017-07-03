/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
function TP(args){
    if(args.length == 3){
        var world;
        var x = player.getX(), y = player.getY(), z = player.getZ();
        world = player.getWorld();
        x = args[0].contains('~') ? x + parseFloat(args[0].subString(args[0].indexOf('~')) : parseFloat(args[0]));
        y = args[1].contains('~') ? y + parseFloat(args[1].subString(args[1].indexOf('~')) : parseFloat(args[1]));
        z = args[2].contains('~') ? z + parseFloat(args[2].subString(args[2].indexOf('~')) : parseFloat(args[2]));
        
        player.teleport(new Location(world, x, y, z));
        
        return null;
    }else if(args.length == 4){
        var player = Bukkit.getPlayer(args[0]);
        if(player == null){
            print("Teleport Cancelled. Player "+args[0]+" does not exist.");
            
            return Executor.STOP;
        }
    	
    	var world;
        var x = player.getX(), y = player.getY(), z = player.getZ();
        world = player.getWorld();
        x = args[1].contains('~') ? x + parseFloat(args[1].subString(args[1].indexOf('~')) : parseFloat(args[1]));
        y = args[2].contains('~') ? y + parseFloat(args[2].subString(args[2].indexOf('~')) : parseFloat(args[2]));
        z = args[3].contains('~') ? z + parseFloat(args[3].subString(args[3].indexOf('~')) : parseFloat(args[3]));
        
        player.teleport(new Location(world, x, y, z));
        
        return null;
    }else{
        print("Teleport Cancelled. Invalid arguments");
        
        return Executor.STOP;
    }
}