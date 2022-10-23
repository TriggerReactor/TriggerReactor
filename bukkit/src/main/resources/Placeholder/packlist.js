/*******************************************************************************
 *     Copyright (C) 2018 wysohn (idea provided by gerzytet, author Pro_Snape)
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

var Arrays = Java.type('java.util.Arrays')

function packlist(args) {
  var type;
  var values = []

  if (args.length >= 1) {
    type = args[0].toLowerCase();
    type = type.charAt(0).toUpperCase() + type.substring(1);

    if (type !== 'String' && type !== 'Int' && type !== 'Double')
      throw new Error("Unknown Type '"+type+" ! You can use either 'String', or 'Int', or 'Double' ! [invalid type]")
  } else {
    throw new Error('$packlist placeholder should have at least 1 arguments!')
  }

  if (args.length >= 2)
    for (var i = 1; i < args.length; i++)
      values.push(args[i]);

  return Java.to(values, 'java.lang.'+type+'[]');
}
