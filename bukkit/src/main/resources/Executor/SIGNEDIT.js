/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
 function SIGNEDIT(args) {
	if(args.length == 3 || args.length == 5){ 
		var lineNumber = args[0];
		var lineText = args[1];
		var location;

		if(args.length == 3){
			location = args[2];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[2], args[3], args[4]);
		}

		Block = location.getBlock();

		if(Block.getType().name().toLowerCase().contains("sign")){
			Sign = Block.getState();
			Sign.setLine(parseInt(lineNumber), ChatColor.translateAlternateColorCodes(Char('&'), lineText));
			Sign.update();
		}else{
			throw new Error(
				'Invalid sign. That block is not a valid sign!');
		}

	}else {
		throw new Error(
			'Invalid parameters. Need [Line<number>, Text<string>, Location<location or number number number>]');
	}
	return null;
}