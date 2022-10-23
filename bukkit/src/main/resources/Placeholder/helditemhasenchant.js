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

var Enchantment = Java.type('org.bukkit.enchantments.Enchantment');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'enchant' }
    ],
    [
      { type: 'string', name: 'enchant' },
      { type: 'int', minimum: 0, name: 'level' }
    ]
  ]
}

function helditemhasenchant(args) {
  if (!player)
    throw new Error('Player is null.');

  var item = player.getItemInHand();

  if (!item)
    return false;

  var enchantmentStr = args[0];

  var enchantment = Enchantment.getByName(enchantmentStr.toUpperCase());
  var level = -1;

  if (!enchantment)
    throw new TypeError('No such enchantment name like' + enchantmentStr);

  if (overload === 1)
    level = args[1]

  var itemMeta = item.getItemMeta();
  if (!itemMeta)
    return false;

  var enchantmentLevel = itemMeta.getEnchantLevel(enchantment);

  return enchantmentLevel > 0 && enchantmentLevel === level;
}