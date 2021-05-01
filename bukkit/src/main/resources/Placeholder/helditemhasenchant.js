/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
function helditemhasenchant(args) {
    if(player == null)
        return null;

    if(player.getItemInHand() == null)
        return false;

    if(args.length < 1)
        throw new Error("Invalid parameter! [String]");

    var Enchantment = Java.type('org.bukkit.enchantments.Enchantment');

    if(typeof args[0] !== "string")
        throw new Error("Invalid parameter! helditemhasenchant accepts 'String' as first paramter.");

    var ench = Enchantment.getByName(args[0].toUpperCase());
    var level = 0;

    if(args.length != 1) {
        if(typeof args[1] !== "number")
            throw new Error("Invalid parameter! helditemhasenchant accepts 'Number' as second paramter.");

        level = Math.max(0, args[1]);
    }

    var itemMeta = player.getItemInHand().getItemMeta();
    if(itemMeta == null)
        return false;

    var enchantMap = itemMeta.getEnchants();

    if(level == 0) {
        return enchantMap.containsKey(ench);
    } else {
        var enchLevel = enchantMap.get(ench);
        if(enchLevel == null)
            return false;

        return enchLevel == level;
    }
}