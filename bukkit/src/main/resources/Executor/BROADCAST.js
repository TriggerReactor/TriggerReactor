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
	var str = "";
	for (var i = 0; i < args.length; i++)
		str += args[i];

	str = ChatColor.translateAlternateColorCodes(Char('&'), str);

	var PlaceholderAPI;
	if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
		PlaceholderAPI = Java.type('me.clip.placeholderapi.PlaceholderAPI');
	}

	var players = BukkitUtil.getOnlinePlayers();
	for (var iter = players.iterator(); iter.hasNext();) {
		p = iter.next();
		if (PlaceholderAPI) {
			p.sendMessage(PlaceholderAPI.setPlaceholders(p, str));
		} else {
			p.sendMessage(str);
		}
	}

	return null;
}