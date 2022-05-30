/*******************************************************************************
 *     Copyright (C) 2018 TheBestNightSky
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
function ACTIONBAR(args) {
	if(args.length == 1){
		var ComponentBuilder = Java.type('net.md_5.bungee.api.chat.ComponentBuilder');
		var component = new ComponentBuilder(args[0]).create();
		var ChatMessageType = Java.type('net.md_5.bungee.api.ChatMessageType');
		var type = ChatMessageType.ACTION_BAR;
		
		player.spigot().sendMessage(type,component);
		return null;
	}
	
	if(args.length == 2){
		var p = args[0];
		var ComponentBuilder = Java.type('net.md_5.bungee.api.chat.ComponentBuilder');
		var component = new ComponentBuilder(args[1]).create();
		var ChatMessageType = Java.type('net.md_5.bungee.api.ChatMessageType');
		var type = ChatMessageType.ACTION_BAR;
		
		p.spigot().sendMessage(type,component);
		return null;
	}
}