/*******************************************************************************
 *     Copyright (C) 2018 wysohn (idea provided by League_Lugas, authored by Pro_Snape)
 *     Copyright (C) 2022 Ioloolo
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

function firstgroup(args) {
  if (!player)
    throw new Error('Player is null.');

  if (!vault)
    throw new Error('Server has no Vault plugin.')

  var groups = vault.permission().getPlayerGroups(null, player);
  var firstGroup = 'None';

  if (groups.length > 0)
    firstGroup = groups[0];

  return firstGroup;
}
