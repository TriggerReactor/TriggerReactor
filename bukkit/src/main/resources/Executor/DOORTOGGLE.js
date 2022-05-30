/*******************************************************************************
 *     Copyright (C) 2017 soliddanii
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
 function DOORTOGGLE(args) {
	if (args.length == 1 || args.length == 3) {
		var location;

		if(args.length == 1){
			location = args[0];
		}else{
			var world = player.getWorld();          
			location = new Location(world, args[0], args[1], args[2]);
		}

		try{
			Block = location.getBlock();
			BlockState = Block.getState();
			Openable = BlockState.getData();
			if(Openable.isOpen()){
				Openable.setOpen(false);
			}else{
				Openable.setOpen(true);
			}
			
			BlockState.setData(Openable);
			BlockState.update();

		}catch(err){
			throw new Error(
				'Invalid door. That block is not a valid door!');
		}
        

	}else {
		throw new Error(
			'Invalid parameters. Need [Location<location or number number number>]');
	}
	return null;
}