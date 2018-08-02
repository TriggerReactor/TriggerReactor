function TIME(args){
	if(args.length != 2)
		throw new Error("Invalid parameters! [String, Number]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "number")
		throw new Error("Invalid parameters! [String, Number]");
		
	var world = Sponge.getServer().getWorld(args[0]).orElse(null);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);
	
	world.getProperties().setWorldTime(args[1]);
}