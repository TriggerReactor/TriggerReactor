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
var Explosion = Java.type('org.spongepowered.api.world.explosion.Explosion')
var Location = Java.type('org.spongepowered.api.world.Location')
var Sponge = Java.type('org.spongepowered.api.Sponge')

function EXPLOSION(args) {
  if (args.length < 4) throw new Error('Invalid parameters! [String, Number, Number, Number]')

  if (
    typeof args[0] !== 'string' ||
    typeof args[1] !== 'number' ||
    typeof args[2] !== 'number' ||
    typeof args[3] !== 'number'
  )
    throw new Error('Invalid parameters! [String, Number, Number, Number]')

  var power = 4.0
  if (args.length > 4) {
    if (typeof args[4] !== 'number') throw new Error('fifth parameter should be a number.')
    else if (args[4] < 0) throw new Error('power should not be negative')
    else power = args[4]
  }

  var fire = false
  if (args.length > 5) {
    if (typeof args[5] !== 'boolean') throw new Error('sixth parameter should be boolean.')
    else fire = args[5]
  }

  var world = Sponge.getServer().getWorld(args[0]).orElse(null)
  if (world == null) throw new Error('Unknown world named ' + args[0])

  world.triggerExplosion(
    Explosion.builder()
      .location(new Location(world, args[1], args[2], args[3]))
      .radius(power)
      .canCauseFire(fire)
      .shouldBreakBlocks(true)
      .shouldDamageEntities(true)
      .shouldPlaySmoke(true)
      .build()
  )
}
