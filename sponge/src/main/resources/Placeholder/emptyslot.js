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
function emptyslot(args) {
    if(player == null)
        return null;

    var hotbarIter = player.getInventory().getHotbar().slots().iterator();
    var inventoryIter = player.getInventory().getMainGrid().slots().iterator();
    var index = 0;
    var found = false;

    while(hotbarIter.hasNext()) {
        if(!hotbarIter.next().peek().isPresent()) {
            found = true;
            break;
        }
        index++;
        continue;
    }
    if(!found) {
        while(inventoryIter.hasNext()) {
            if(!inventoryIter.next().peek().isPresent()) {
                found = true;
                break;
            }
            index++;
            continue;
        }
    }

    return found ? index : -1;
}