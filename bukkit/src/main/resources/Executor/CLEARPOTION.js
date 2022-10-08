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

var PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');

var validation = {
  overloads: [
    [],
    [
      { type: 'string', name: 'effect' }
    ]
  ]
};

function CLEARPOTION(args) {
  if (!player) throw new Error('Player is null.');

  if (overload === 0) {
    var activePotionEffects = player.getActivePotionEffects();
    var iter = activePotionEffects.iterator();
    while (iter.hasNext()) {
      var effectType = iter.next().getType();

      player.removePotionEffect(effectType);
    }
  } else if (overload === 1) {
    var potion = PotionEffectType.getByName(args[0].toUpperCase());

    if (!potion) throw new Error(args[0] + ' is not a valid potion.');

    player.removePotionEffect(
      PotionEffectType.getByName(potion)
    );
  }

  return null;
}
