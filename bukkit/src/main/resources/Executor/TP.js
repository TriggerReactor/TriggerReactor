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
        var x, y, z;
        world = player.getWorld();
        x = args[0];
        y = args[1];
        z = args[2];
        
        player.teleport(new Location(world, x, y, z));
        
        return null;
    }else if(args.length == 4){
        var world;
        var x, y, z;
        world = player.getWorld();
        x = args[0];
        y = args[1];
        z = args[2];
        
        var target = Bukkit.getPlayer(args[3]);
        target.teleport(new Location(world, x, y, z));
        
        return null;
    }else if(args.length == 1){
        var loc = args[0];
        player.teleport(loc);
        
        return null;
    }else if (args.length == 5) {
		var world;
        var x, y, z, yaw, pitch;
        world = player.getWorld();
        x = args[0];
        y = args[1];
        z = args[2];
		yaw = args[3];
		pitch = args[4];
        
        player.teleport(new Location(world, x, y, z, yaw, pitch));
        
        return null;
	}else{
        print("Teleport Cancelled. Invalid arguments");
        
        return Executor.STOP;
  }
}