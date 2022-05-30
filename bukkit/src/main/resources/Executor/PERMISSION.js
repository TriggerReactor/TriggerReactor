/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
function PERMISSION(args){
	if(player === null)
		return null;
		
	if(args.length != 1 || typeof args[0] !== "string")
		throw new Error("Invalid parameter! [String]")

	if(args[0].length > 1 && args[0].charAt(0) == '-')
		vault.revoke(player, args[0].substring(1));
	else
		vault.permit(player, args[0]);
}