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

var Player = Java.type('org.bukkit.entity.Player');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'permission' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'permission' }
    ]
  ]
};

function PERMISSION(args) {
  var target, permission;

  if (overload === 0) {
    target = player;
    permission = args[0];
  } else if (overload === 1) {
    target = args[0];
    permission = args[1];
  }

  if (!vault) throw new Error('Vault is not available.');
  if (!target) throw new Error('Player is null.');

  if (permission.startsWith('-')) vault.revoke(target, permission.substring(1));
  else vault.permit(target, permission);

  return null;
}
