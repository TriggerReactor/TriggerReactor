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
			Sign.setLine(parseInt(lineNumber), replaceColorCodes(lineText));
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


function replaceColorCodes(string){

	string = replaceAll(string, '&0', '\u00A70');
	string = replaceAll(string, '&1', '\u00A71');
	string = replaceAll(string, '&2', '\u00A72');
	string = replaceAll(string, '&3', '\u00A73');
	string = replaceAll(string, '&4', '\u00A74');
	string = replaceAll(string, '&5', '\u00A75');
	string = replaceAll(string, '&6', '\u00A76');
	string = replaceAll(string, '&7', '\u00A77');
	string = replaceAll(string, '&8', '\u00A78');
	string = replaceAll(string, '&9', '\u00A79');
	string = replaceAll(string, '&a', '\u00A7a');
	string = replaceAll(string, '&b', '\u00A7b');
	string = replaceAll(string, '&c', '\u00A7c');
	string = replaceAll(string, '&d', '\u00A7d');
	string = replaceAll(string, '&e', '\u00A7e');
	string = replaceAll(string, '&f', '\u00A7f');
	string = replaceAll(string, '&k', '\u00A7k');
	string = replaceAll(string, '&l', '\u00A7l');
	string = replaceAll(string, '&m', '\u00A7m');
	string = replaceAll(string, '&n', '\u00A7n');
	string = replaceAll(string, '&o', '\u00A7o');
	string = replaceAll(string, '&r', '\u00A7r');

	return string;
}

function replaceAll(str, find, replace) {
	return str.replace(new RegExp(find, 'g'), replace);
}