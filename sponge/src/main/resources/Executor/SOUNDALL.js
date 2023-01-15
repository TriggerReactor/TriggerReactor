/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
var SoundType = Java.type('org.spongepowered.api.effect.sound.SoundType')
var SoundTypes = Java.type('org.spongepowered.api.effect.sound.SoundTypes')
var ReflectionUtil = Java.type('io.github.wysohn.triggerreactor.tools.ReflectionUtil')
var Vector3d = Java.type('com.flowpowered.math.vector.Vector3d')

function SOUNDALL(args) {
  if (args.length == 4) {
    var location = args[0]
    var volume = args[2]
    var pitch = args[3]

    var sound
    try {
      sound = ReflectionUtil.getField(SoundTypes.class, null, args[1])
    } catch (ex) {
      sound = SoundType.of(args[1])
    } finally {
      location.getExtent().playSound(sound, new Vector3d(location.getPosition()), volume, pitch)
    }
  } else {
    throw new Error('Invalid parameters. Need [Location, Sound, Number, Number]')
  }
  return null
}
