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

var ChatColor = Java.type('org.bukkit.ChatColor');
var ArrayList = Java.type('java.util.ArrayList')

var validation = {
  overloads: [
    [
      { type: 'string', name: 'type', matches: 'TITLE' },
      { type: 'string', name: 'title' }
    ],
    [
      { type: 'string', name: 'type', matches: 'LORE' },
      { type: 'string', name: 'subType', matches: 'ADD' },
      { type: 'string', name: 'lore' }
    ],
    [
      { type: 'string', name: 'type', matches: 'LORE' },
      { type: 'string', name: 'subType', matches: 'ADD' },
      { type: 'int', name: 'index' },
      { type: 'string', name: 'lore' }
    ],
    [
      { type: 'string', name: 'type', matches: 'LORE' },
      { type: 'string', name: 'subType', matches: 'SET' },
      { type: 'int', name: 'index' },
      { type: 'string', name: 'lore' }
    ],
    [
      { type: 'string', name: 'type', matches: 'LORE' },
      { type: 'string', name: 'subType', matches: 'REMOVE' },
      { type: 'int', name: 'index' }
    ]
  ]
};

function MODIFYHELDITEM(args) {
  if (!player) throw new Error('Player is null.');

  var item = player.getInventory().getItemInHand();

  if (!item || item.getType().name === 'AIR')
    throw new Error('Held item is null or air.');

  var meta = item.getItemMeta();

  if (overload === 0) {
    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', args[1]));
  } else {
    var lore = meta.getLore();
	if (!lore) lore = new ArrayList();
    if (overload === 1) lore.add(ChatColor.translateAlternateColorCodes('&', args[2]));
    else if (overload === 2) lore.add(args[2], ChatColor.translateAlternateColorCodes('&', args[3]));
    else if (overload === 3) lore.set(args[2], ChatColor.translateAlternateColorCodes('&', args[3]));
    else if (overload === 4) lore.set(ChatColor.translateAlternateColorCodes('&', args[2]));

    meta.setLore(lore);
  }

  item.setItemMeta(meta);
  player.getInventory().setItemInHand(item);

  return null;
}
