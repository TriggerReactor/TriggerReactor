/*******************************************************************************
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
      { type: 'number', minimum: 0, maximum: 1, name: 'xp' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'number', minimum: 0, maximum: 1, name: 'xp' }
    ]
  ]
};

function SETXP(args) {
  var target, xp;

  if (overload === 0) {
    target = player;
    xp = args[0];
  } else if (overload === 1) {
    target = args[0];
    xp = args[1];
  }

  if (!target) throw new Error('Player is null.');

  target.setExp(xp);

  return null;
}
