function blockname(args){
	
	if ((args.length == 0) || (args.length == 1) || (args.length == 2) || (args.length == 3)) {
		throw new Error("$blockname placeholder should be used as $blockname:\"worldname\": x: y: z.");
	}else if (args.length == 4) {
		
		if (Bukkit.getWorld(args[0]) == null) {
			throw new Error("The world does not exist.");
			return null;
		}
		if ((typeof args[1] !== "number")||(typeof args[2] !== "number")||(typeof args[3] !== "number")) {
			throw new Error("Coordinate values must be integers.");
			return null;
		}
		world = Bukkit.getWorld(args[0]);
		x = args[1];
		y = args[2];
		z = args[3];
		
		block = world.getBlockAt(x,y,z).getType().name().toLowerCase();
		return block;
	}
}