/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
var Sponge = Java.type('org.spongepowered.api.Sponge')
var Vector3d = Java.type('com.flowpowered.math.vector.Vector3d')

function PUSH(args) {
  if (args.length == 4) {
    var entity = args[0]
    var motionX = args[1]
    var motionY = args[2]
    var motionZ = args[3]

    if (typeof entity == 'string') {
      entity = Sponge.getServer().getPlayer(entity).orElse(null)
    }

    entity.setVelocity(new Vector3d(motionX.toFixed(2), motionY.toFixed(2), motionZ.toFixed(2)))
  } else {
    throw new Error('Invalid parameters. Need [Entity<entity or string>, Number, Number, Number]')
  }
  return null
}
