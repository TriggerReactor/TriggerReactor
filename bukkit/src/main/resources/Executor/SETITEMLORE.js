/*******************************************************************************
 *     Copyright (C) 2019 Pro_Snape
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

var ItemStack = Java.type('org.bukkit.inventory.ItemStack');
var ChatColor = Java.type('org.bukkit.ChatColor');

var validation = {
  overloads: [
    [
      { type: 'string', name: 'lore' },
      { type: ItemStack.class, name: 'item' }
    ]
  ]
};

function SETITEMLORE(args) {
  var lores = ChatColor.translateAlternateColorCodes('&', args[0]).split('\n');
  var item = args[1];

  if (!item || item.getType().name === 'AIR')
    throw new Error('Item is null or air.');

  var meta = item.getItemMeta();
  meta.setLore(lores);
  item.setItemMeta(meta);

  return null;
}
