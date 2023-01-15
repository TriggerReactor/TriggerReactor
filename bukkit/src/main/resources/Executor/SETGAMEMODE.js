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
var GameMode = Java.type('org.bukkit.GameMode');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'mode' }
    ],
    [
      { type: 'int', name: 'mode' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'mode' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'int', name: 'food' }
    ]
  ]
};

function SETGAMEMODE(args) {
  var target, modeStr;

  if (overload === 0) {
    target = player;
    modeStr = args[0];
  } else if (overload === 1) {
    target = player;
    modeStr = intToModeStr(args[0]);
  } else if (overload === 2) {
    target = args[0];
    modeStr = args[1];
  } else if (overload === 3) {
    target = args[0];
    modeStr = intToModeStr(args[1]);
  }

  var mode;

  try {
    mode = GameMode.valueOf(modeStr);
  } catch (_) {
    throw new Error('"' + modeStr + '" is not valid GameMode type.');
  }

  target.setGameMode(mode);

  return null;
}

function intToModeStr(num) {
  switch (num) {
    case 0:
      return 'SURVIVAL';
    case 1:
      return 'CREATIVE';
    case 2:
      return 'ADVENTURE';
    case 3:
      return 'SPECTATOR';
    default:
      return '';
  }
}
