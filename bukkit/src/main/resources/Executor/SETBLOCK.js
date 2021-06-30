/*******************************************************************************
 *     Copyright (C) 2021 soliddanii and contributors
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

var Location = Java.type('org.bukkit.Location');
function SETBLOCK(args) {
    if (typeof block !== 'undefined' && (args.length == 1 || args.length == 2)) {
        var blockID = args[0];
        var blockData = args.length  == 2 ? args[1] : 0;

        setBlock(block, blockID, blockData);
    } else if (args.length == 2 || args.length == 4) {
        var blockID = args[0];
        var location;

        if (args.length == 2) {
            location = args[1];
        } else {
            if(typeof player === 'undefined')
                throw new Error('cannot use #SETBLOCK in non-player related event. Or use Location instance.')

            var world = player.getWorld();
            location = new Location(world, args[1], args[2], args[3]);
        }

        block = location.getBlock();

        setBlock(block, blockID, 0);
    } else if (args.length == 3 || args.length == 5) {
        var blockID = args[0];
        var blockData = args[1];
        var location;

        if (args.length == 3) {
            location = args[2];
        } else {
            var world = player.getWorld();
            location = new Location(world, args[2], args[3], args[4]);
        }

        block = location.getBlock();

        setBlock(block, blockID, blockData);
    } else {
        throw new Error(
            'Invalid parameters. Need [Block<string or number>, Location<location or number number number>] or [Block<string or number>, BlockData<number>, Location<location or number number number>]');
    }
    return null;
}

function setBlock(block, blockId, blockData){
    var Material = Java.type('org.bukkit.Material');
    var legacy = typeof block.setData === 'function';

    var mat = null;
    if(typeof blockId === 'number' && (blockId % 1) === 0){
        if(!legacy)
            throw new Error("Cannot use a number as block type after 1.12.2. Use material name directly.");

        mat = Material.getMaterial(blockId);
    } else {
        mat = Material.valueOf(blockId.toUpperCase());
    }
    block.setType(mat);

    if(legacy){
        block.setData(blockData);
    }
}