/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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

var validation = {
  overloads: [
    [],
    [
      { type: 'int', minimum: 0, name: 'fromIndex' }
    ],
    [
      { type: 'int', minimum: 0, name: 'fromIndex' },
      { type: 'int', minimum: 0, name: 'toIndex' }
    ]
  ]
}

function cmdline(args) {
  var messages = event.getMessage().substring(1).split(' ');

  var from = 0;
  var to = messages.length - 1;

  if (overload === 1) {
    from = args[0];
  } else if (overload === 2) {
    if (args[0] > args[1]) {
      from = args[1];
      to = args[0];
    } else {
      from = args[0];
      to = args[1];
    }
  }

  return messages.slice(from, to+1).join(' ');
}