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
var BlockTypes = Java.type('org.spongepowered.api.block.BlockTypes')
var Keys = Java.type('org.spongepowered.api.data.key.Keys')
var Text = Java.type('org.spongepowered.api.text.Text')
var Location = Java.type('org.spongepowered.api.world.Location')
var ArrayList = Java.type('java.util.ArrayList')

function SIGNEDIT(args) {
  if (args.length == 3 || args.length == 5) {
    var lineNumber = parseInt(args[0])
    var lineText = args[1]
    var location

    if (lineNumber < 0 || lineNumber > 3) {
      throw new Error('line should be within 0 and 3!')
    }

    if (args.length == 3) {
      location = args[2]
    } else {
      var world = player.getWorld()
      location = new Location(world, args[2], args[3], args[4])
    }

    if (
      location.getBlockType() != BlockTypes.STANDING_SIGN &&
      location.getBlockType() != BlockTypes.WALL_SIGN
    )
      throw new Error('That block is not a valid sign!')

    var lines = location.get(Keys.SIGN_LINES).orElse(new ArrayList())

    while (lines.size() < 4) {
      lines.add(Text.of(''))
    }

    lines.set(lineNumber, Text.of(lineText))
    location.offer(Keys.SIGN_LINES, lines)
  } else {
    throw new Error(
      'Invalid parameters. Need [Line<number>, Text<string>, Location<location or number number number>]'
    )
  }
  return null
}
