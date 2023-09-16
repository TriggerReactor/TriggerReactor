/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
var MessageChannel = Java.type('org.spongepowered.api.text.channel.MessageChannel')
var Text = Java.type('org.spongepowered.api.text.Text')
var Sponge = Java.type('org.spongepowered.api.Sponge')
var TextUtil = Java.type('io.github.wysohn.triggerreactor.sponge.tools.TextUtil')
var String = Java.type('java.lang.String')

function BROADCAST(args) {
  for (var i = 0; i < args.length; i++) {
    var message;
    if (args[i] instanceof Text) {
      message = args[i];
    } else {
      message = TextUtil.colorStringToText(String.valueOf(args[i]));
    }

    MessageChannel.TO_PLAYERS.send(message);
  }

  return null;
}
