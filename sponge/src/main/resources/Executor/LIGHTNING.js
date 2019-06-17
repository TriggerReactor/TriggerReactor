function LIGHTNING(args){
	if(args.length != 4)
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "number" 
		|| typeof args[2] !== "number"
		|| typeof args[3] !== "number")
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	var world = Sponge.getServer().getWorld(args[0]).orElse(null);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);
	
	var location = new Location(world, args[1], args[2], args[3]);
	var lightning = world.createEntity(EntityTypes.LIGHTNING, location.getPosition());
	world.spawnEntity(lightning);
}