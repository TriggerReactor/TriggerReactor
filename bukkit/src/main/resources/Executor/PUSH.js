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

var Vector = Java.type('org.bukkit.util.Vector');
var Entity = Java.type('org.bukkit.entity.Entity');

var validation = {
  overloads: [
    [
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' }
    ],
    [
      { type: Entity.class, name: 'entity' },
      { type: 'number', name: 'x' },
      { type: 'number', name: 'y' },
      { type: 'number', name: 'z' }
    ]
  ]
};

function PUSH(args) {
  var target, x, y, z;

  if (overload === 0) {
    target = player;
    x = args[0];
    y = args[1];
    z = args[2];
  } else if (overload === 1) {
    target = args[0];
    x = args[1];
    y = args[2];
    z = args[3];
  }

  if (!target) return null;

  var vector = new Vector(x.toFixed(2), y.toFixed(2), z.toFixed(2));

  target.setVelocity(vector);

  return null;
}
