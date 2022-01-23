/*******************************************************************************
 *     Copyright (C) 2018 TheBestNightSky
 *     Copyright (C) 2022 Sayakie
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
var Player = Java.type('org.spongepowered.api.entity.living.player.Player');
var ChatTypes = Java.type('org.spongepowered.api.text.chat.ChatTypes');
var Sponge = Java.type('org.spongepowered.api.Sponge');

function ACTIONBAR(args) {
    var target, message;

	if (args.length == 1) {
	    target = player;
	    message = args[0];
	} else if (args.length == 2) {
		target = args[0];
		message = args[1];

		if (!(target instanceof Player)) {
		    target = Sponge.getGame().getServer().getPlayer(args[0]).orElseThrow(UnexpectedTargetException)
		}
	}

	if (target && message) {
	    target.sendMessage(ChatTypes.ACTION_BAR, TextUtil.colorStringToText(message));
	    return null;
	}

	throw new Error("Invalid arguments.")
}

function UnexpectedTargetException() {
    throw new Error("Could not found a player. May they be offline.")
}