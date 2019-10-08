function LIGHTNING(args){
	if(args.length != 4)
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "number" 
		|| typeof args[2] !== "number"
		|| typeof args[3] !== "number")
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	var world = Bukkit.getWorld(args[0]);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);
	
	var Location = Java.type('org.bukkit.Location');
	world.strikeLightning(new Location(world, args[1], args[2], args[3]));
}