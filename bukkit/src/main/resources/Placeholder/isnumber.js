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
    [
      { type: 'number', name: 'target' }
    ],
    [
      { type: 'string', name: 'target' }
    ]
  ]
};

function isnumber(args) {
  if (overload === 0)
    return true;

  var target = args[0];
  var regExp = /^[-]{0,1}[0-9]+[.]{0,1}(?:[0-9]+){0,1}$/

  return regExp.test(target)
}
