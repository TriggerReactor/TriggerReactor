/*******************************************************************************
 *     Copyright (C) 2017 wysohn
 *     Copyright (C) 2022 Sayakie
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
function CLEARENTITY(args) {
  if (!player) return null
  if (args.length != 1 || typeof args[0] !== 'number')
    throw new Error('Invalid parameters! [Number]')

  var entities = player.getLocation().getExtent().getEntities()

  for (var i = 0; i < entities.size(); i++) {
    var entity = entities[i]

    if (entity == player) continue

    var dist = entity.getLocation().getPosition().distance(player.getLocation().getPosition())
    if (dist < args[0]) {
      entity.remove()
    }
  }
}
