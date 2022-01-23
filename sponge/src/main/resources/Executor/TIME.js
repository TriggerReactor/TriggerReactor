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
var Sponge = Java.type('org.spongepowered.api.Sponge')

validation = {
  overloads: [
    [
      { name: 'world', type: 'string' },
      { name: 'time', type: 'int' }
    ]
  ]
}

function TIME(args) {
  if (overload === 0) {
    var world = Sponge.getServer().getWorld(args[0]).orElse(null)
    if (world == null) throw new Error('Unknown world named ' + args[0])

    world.getProperties().setWorldTime(args[1])
    return null
  }
}
