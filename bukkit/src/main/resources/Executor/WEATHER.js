function WEATHER(args){
	if(args.length !== 2)
		throw new Error("Invalid parameters! [String, Boolean]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "boolean")
		throw new Error("Invalid parameters! [String, Boolean]");
		
	var world = Bukkit.getWorld(args[0]);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);
	
	world.setStorm(args[1]);
}