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

var Damageable = Java.type('org.bukkit.entity.Damageable');

var validation = {
  overloads: [
    [],
    [{ type: Damageable.class, name: 'entity' }]
  ]
};

function KILL(args) {
  var target;

  if (overload === 0) target = player;
  else if (overload === 1) target = args[0];

  if (!target) throw new Error('Entity is null.');

  target.setHealth(0);

  return null;
}
