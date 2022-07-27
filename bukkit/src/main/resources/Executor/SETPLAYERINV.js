/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
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
var ItemStack = Java.type('org.bukkit.inventory.ItemStack');

var validation = {
  overloads: [
    [
      { name: 'slot', type: 'int', minimum: 0, maximum: 35 },
      { name: 'item', type: ItemStack.class }
    ],
    [
      { name: 'player', type: Player.class },
      { name: 'slot', type: 'int', minimum: 0, maximum: 35 },
      { name: 'item', type: ItemStack.class }
    ]
  ]
};

function SETPLAYERINV(args) {
  var target, slot, item;

  if (overload === 0) {
    target = player;
    slot = args[0];
    item = args[1];
  } else if (overload === 1) {
    target = args[0];
    slot = args[1];
    item = args[2];
  }

  if (!target) throw new Error('Player is null.');

  target.getInventory().setItem(slot, item);

  return null;
}
