/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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

var validation = {
  overloads: [
    [
      { type: 'int', minimum: 0, name: 'index' }
    ]
  ]
};

function helditemlore(args) {
  if (!player)
    throw new Error('Player is null.');

  if (!player.getItemInHand())
    return '';

  var itemMeta = player.getItemInHand().getItemMeta();
  if (!itemMeta)
    return '';

  var lores = itemMeta.getLore();
  if (!lores)
    return '';

  var index = args[0];
  if (index >= lores.size())
    return '';

  return lores[index];
}