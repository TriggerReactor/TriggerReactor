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
function TPPOS(args){
    args = args[0].split(" ");

    if(args.length == 3){
        var world = player.getLocation().getWorld();
        var x = player.getLocation().getX(), y = player.getLocation().getY(), z = player.getLocation().getZ();
        x = x + (args[0].contains('~') ? parseFloat(args[0].substring(args[0].indexOf('~')+1)) : parseFloat(args[0]));
        y = y + (args[1].contains('~') ? parseFloat(args[1].substring(args[1].indexOf('~')+1)) : parseFloat(args[1]));
        z = z + (args[2].contains('~') ? parseFloat(args[2].substring(args[2].indexOf('~')+1)) : parseFloat(args[2]));
        
        player.teleport(new Location(world, x, Math.min(256, Math.max(-10 ,y)), z));
        
        return null;
    }else if(args.length == 4){
        player = Bukkit.getPlayer(args[3]);
        if(player == null){
            print("Teleport Cancelled. Player "+args[3]+" does not exist.");
            
            return Executor.STOP;
        }
    	
        var world = player.getLocation().getWorld();
        var x = player.getLocation().getX(), y = player.getLocation().getY(), z = player.getLocation().getZ();
        x = x + (args[0].contains('~') ? parseFloat(args[0].substring(args[0].indexOf('~')+1)) : parseFloat(args[0]));
        y = y + (args[1].contains('~') ? parseFloat(args[1].substring(args[1].indexOf('~')+1)) : parseFloat(args[1]));
        z = z + (args[2].contains('~') ? parseFloat(args[2].substring(args[2].indexOf('~')+1)) : parseFloat(args[2]));
        
        player.teleport(new Location(world, x, Math.min(256, Math.max(-10 ,y)), z));
        
        return null;
    }else{
        print("Teleport Cancelled. Invalid arguments");
        
        return Executor.STOP;
    }
}