/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
var Keys = Java.type('org.spongepowered.api.data.key.Keys')

function exp(args) {
  if (player == null) return null

  var currentLevel = player.get(Keys.EXPERIENCE_LEVEL).orElse(0)
  var requiredExp
  if (currentLevel >= 0 && currentLevel <= 15) {
    requiredExp = 2 * currentLevel + 7
  } else if (currentLevel >= 16 && currentLevel <= 30) {
    requiredExp = 5 * currentLevel - 38
  } else if (currentLevel >= 31) {
    requiredExp = 9 * currentLevel - 158
  }
  var currentExp = player.get(Keys.EXPERIENCE_SINCE_LEVEL).orElse(0)
  var forRound = (currentExp / requiredExp) * 100
  var actualValue = Math.round(forRound) / 100
  return actualValue
}
