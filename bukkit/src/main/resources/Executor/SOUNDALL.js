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

var Sound = Java.type("org.bukkit.Sound");
var Location = Java.type("org.bukkit.Location");

var validation = {
  overloads: [
    [
      { type: Location.class, name: "location" },
      { type: "string", name: "sound" },
    ],
    [
      { type: Location.class, name: "location" },
      { type: "string", name: "sound" },
      { type: "number", name: "volume" },
    ],
    [
      { type: Location.class, name: "location" },
      { type: "string", name: "sound" },
      { type: "number", name: "volume" },
      { type: "number", name: "pitch" },
    ],
    [
      { type: Location.class, name: "location" },
      { type: Sound.class, name: "sound" },
    ],
    [
      { type: Location.class, name: "location" },
      { type: Sound.class, name: "sound" },
      { type: "number", name: "volume" },
    ],
    [
      { type: Location.class, name: "location" },
      { type: Sound.class, name: "sound" },
      { type: "number", name: "volume" },
      { type: "number", name: "pitch" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: "string", name: "sound" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: "string", name: "sound" },
      { type: "number", name: "volume" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: "string", name: "sound" },
      { type: "number", name: "volume" },
      { type: "number", name: "pitch" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: Sound.class, name: "sound" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: Sound.class, name: "sound" },
      { type: "number", name: "volume" },
    ],
    [
      { type: "int", name: "x" },
      { type: "int", name: "y" },
      { type: "int", name: "z" },
      { type: Sound.class, name: "sound" },
      { type: "number", name: "volume" },
      { type: "number", name: "pitch" },
    ],
  ],
};

function toEnumOrString(str) {
  try {
    var sound = Sound.valueOf(str.toUpperCase());
    return sound;
  } catch (e) {
    var sound = str;
    return sound;
  }
}

function SOUNDALL(args) {
  var location, sound, volume, pitch;

  if (overload === 0) {
    location = args[0];
    sound = toEnumOrString(args[1]);
    volume = 1;
    pitch = 1;
  } else if (overload === 1) {
    location = args[0];
    sound = toEnumOrString(args[1]);
    volume = args[2];
    pitch = 1;
  } else if (overload === 2) {
    location = args[0];
    sound = toEnumOrString(args[1]);
    volume = args[2];
    pitch = args[3];
  } else if (overload === 3) {
    location = args[0];
    sound = args[1];
    volume = 1;
    pitch = 1;
  } else if (overload === 4) {
    location = args[0];
    sound = args[1];
    volume = args[2];
    pitch = 1;
  } else if (overload === 5) {
    location = args[0];
    sound = args[1];
    volume = args[2];
    pitch = args[3];
  } else if (overload === 6) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = toEnumOrString(args[1]);
    volume = 1;
    pitch = 1;
  } else if (overload === 7) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = toEnumOrString(args[1]);
    volume = args[4];
    pitch = 1;
  } else if (overload === 8) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = toEnumOrString(args[1]);
    volume = args[4];
    pitch = args[5];
  } else if (overload === 9) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = args[3];
    volume = 1;
    pitch = 1;
  } else if (overload === 10) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = args[3];
    volume = args[4];
    pitch = 1;
  } else if (overload === 11) {
    location = new Location(
      player ? player.getLocation().getWorld() : Bukkit.getWorld("world"),
      args[0],
      args[1],
      args[2]
    );
    sound = args[3];
    volume = args[4];
    pitch = args[5];
  }

  if (!sound)
    throw new Error(overload >= 6 ? args[3] : args[1] + " is not valid sound.");

  location.getWorld().playSound(location, sound, volume, pitch);

  return null;
}
