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
var PotionEffect = Java.type('org.bukkit.potion.PotionEffect');
var PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'potionName' },
      { type: 'int', minimum: 0, name: 'second' }
    ],
    [
      { type: 'string', name: 'potionName' },
      { type: 'int', minimum: 0, name: 'second' },
      { type: 'int', minimum: 0, name: 'amplifier' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'potionName' },
      { type: 'int', minimum: 0, name: 'second' }
    ],
    [
      { type: Player.class, name: 'player' },
      { type: 'string', name: 'potionName' },
      { type: 'int', minimum: 0, name: 'second' },
      { type: 'int', minimum: 0, name: 'amplifier' }
    ]
  ]
};

function POTION(args) {
  var target, potionName, second, amplifier;

  if (overload === 0) {
    target = player;
    potionName = args[0];
    second = args[1];
    amplifier = 1;
  } else if (overload === 2) {
    target = player;
    potionName = args[0];
    second = args[1];
    amplifier = args[2];
  } else if (overload === 0) {
    target = args[0];
    potionName = args[1];
    second = args[2];
    amplifier = 1;
  } else if (overload === 2) {
    target = args[0];
    potionName = args[1];
    second = args[2];
    amplifier = args[3];
  }

  if (!target) throw new Error('Player is null.');

  second *= 20;

  var effectType = PotionEffectType.getByName(potionName);
  if (!effectType) throw new Error(args[0] + ' is not a valid potion.');

  var effect = new PotionEffect(effectType, second, amplifier);

  target.addPotionEffect(effect);

  return null;
}
