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
var BlockFace = Java.type('org.bukkit.block.BlockFace');

function ROTATEBLOCK(args) {
    var face = BlockFace.valueOf(args[0]);

    if(typeof(block) !== 'undefined' && args.length == 1){
        // nothing
    }else if(args.length == 4){
        location = new Location(player.getWorld(), args[1], args[2], args[3]);
        block = location.getBlock()
    }else if(args.length == 2){
        location = args[1];
        block = location.getBlock()
    }else{
        throw new Error(
            'Invalid parameters. Need [BlockFace<string>] or [BlockFace<string>, Location<location or number number number>]');
    }

    var face = BlockFace.valueOf(args[0]);
    var blockState = block.getState();
    var blockData = blockState.getData();
    blockData.setFacingDirection(face);
    blockState.setData(blockData);

    return null;
}
