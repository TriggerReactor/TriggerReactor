function WEATHER(args){
	if(args.length != 2)
		throw new Error("Invalid parameters! [String, Boolean]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "boolean")
		throw new Error("Invalid parameters! [String, Boolean]");
		
	var world = Sponge.getServer().getWorld(args[0]).orElse(null);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);
	
	world.getProperties().setThundering(args[1]);
	world.getProperties().setRaining(args[1]);
}