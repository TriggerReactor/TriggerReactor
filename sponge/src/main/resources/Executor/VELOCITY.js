/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
var Entity = Java.type('org.spongepowered.api.entity.Entity')
var Location = Java.type('org.spongepowered.api.world.Location')
var Sponge = Java.type('org.spongepowered.api.Sponge')
var Vector3d = Java.type('com.flowpowered.math.vector.Vector3d')

validation = {
  overloads: [
    [
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' }
    ],
    [
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'target', type: Entity.class }
    ],
    [
      { name: 'x', type: 'number' },
      { name: 'y', type: 'number' },
      { name: 'z', type: 'number' },
      { name: 'target', type: 'string' }
    ],
    [{ name: 'location', type: Vector3d.class }],
    [{ name: 'location', type: Location.class }],
    [
      { name: 'location', type: Vector3d.class },
      { name: 'target', type: Entity.class }
    ],
    [
      { name: 'location', type: Vector3d.class },
      { name: 'target', type: 'string' }
    ]
  ]
}

function VELOCITY(args) {
  var target = player,
    location

  if (args.length === 1) {
    if (args[0] instanceof Location) {
      location = args[0].getPosition()
    } else {
      location = args[0]
    }
  } else if (args.length === 2) {
    var targetOrtargetName = args[3]

    if (typeof targetOrtargetName === 'string')
      target = Sponge.getServer().getPlayer(targetOrtargetName).orElse(null)
    else target = targetOrtargetName
    location = args[0]
  } else if (args.length === 3) {
    var x = args[0]
    var y = args[1]
    var z = args[2]

    location = new Vector3d(x, y, z)
  } else if (args.length === 4) {
    var x = args[0]
    var y = args[1]
    var z = args[2]
    var targetOrtargetName = args[3]

    if (typeof targetOrtargetName === 'string')
      target = Sponge.getServer().getPlayer(targetOrtargetName).orElse(null)
    else target = targetOrtargetName
    location = new Vector3d(x, y, z)
  } else {
    throw new Error('Invalid parameters!')
  }

  if (!(target instanceof Entity)) {
    throw new Error('Could not find a player')
  } else if (!(location instanceof Vector3d)) {
    throw new Error('Could not find a target location')
  }

  target.setVelocity(location)
  return null
}
