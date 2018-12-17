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
function BURN(args) {
	var functions = [
		() => { return null },
		() => {
			var seconds = args[0];
			player.setFireTicks(seconds * 20); // NOTE: args[0] is seconds
		},
		() => {
			var entity = args[0]
			var seconds = args[1]

			if (typeof entity === 'string') entity = Bukkit.getPlayer(entity);

			entity.setFireTicks(seconds * 20)
		}
	];
	if (functions.length < args.length) {
		throw new Error('Invalid parameters. Need [Number] or [Entity<entity or string>, Number]');
	} else {
		functions[args.length]();
	};

	return null;
}
