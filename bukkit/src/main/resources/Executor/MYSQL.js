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

var Object = Java.type('java.lang.Object');
var MysqlSupport = Java.type('io.github.wysohn.triggerreactor.tools.mysql.MysqlSupport');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'key' },
      { type: Object.class, name: 'value' }
    ]
  ]
};

function MYSQL(args) {
  var mysqlHelper = injector.getInstance(MysqlSupport.class);

  if (!mysqlHelper) throw new Error('Mysql connection is not available.');

  var key = args[0];
  var value = args[1];

  mysqlHelper.set(key, value);

  return null;
}
