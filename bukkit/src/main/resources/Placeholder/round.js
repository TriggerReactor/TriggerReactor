/* ******************************************************************************
 *     Copyright (C) 2018 wysohn (Created by black9685, professer_snape, RedLime)
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
    [
      { type: 'number', name: 'number' }
    ],
    [
      { type: 'number', name: 'number' },
      { type: 'int', name: 'digit' }
    ]
  ]
}

function round(args) {
  var number = args[0];
  var digit = 0;

  if (overload === 1)
    digit = args[1];

  digit = Math.pow(10, digit);

  return Math.round(number*digit) / digit
}
