/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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

var Player = Java.type("org.bukkit.entity.Player");

var validation = {
  overloads: [
    [{ type: "number", name: "second" }],
    [
      { type: Player.class, name: "player" },
      { type: "number", name: "second" },
    ],
  ],
};

function BURN(args) {
  var target, second;

  if (overload === 0) {
    target = player;
    second = args[0];
  } else if (overload === 1) {
    target = args[0];
    second = args[1];
  }

  if (!target) return null;

  second *= 20;

  target.setFireTicks(second);

  return null;
}
