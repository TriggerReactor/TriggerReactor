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
function BROADCAST(args) {
    var String = Java.type('java.lang.String')
    var Text = Java.type('org.spongepowered.api.text.Text')
    
	for (var i = 0; i < args.length; i++){
        var t;
        if(args[i] instanceof Text)
            t = args[i];
        else
            t = TextUtil.colorStringToText(String.valueOf(args[i]));
            
        var players = Sponge.getServer().getOnlinePlayers();
        for (var iter = players.iterator(); iter.hasNext();) {
            p = iter.next();
            p.sendMessage(t);
        }
	}

	return null;
}