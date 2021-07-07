var Bukkit = Java.type('org.bukkit.Bukkit')
var Location = Java.type('org.bukkit.Location');
var Float = Java.type('java.lang.Float')

function EXPLOSION(args){
	if(args.length < 4)
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	if(typeof args[0] !== "string" 
		|| typeof args[1] !== "number" 
		|| typeof args[2] !== "number"
		|| typeof args[3] !== "number")
		throw new Error("Invalid parameters! [String, Number, Number, Number]");
		
	var power = 4.0;
	if(args.length > 4){
		if(typeof args[4] !== "number")
			throw new Error("fifth parameter should be a number.");
		else if (args[4] < 0)
		{
			throw new Error("power should not be negative")
		}
		else
			power = args[4];
	}
		
	var fire = false;
	if(args.length > 5){
		if(typeof args[5] !== "boolean")
			throw new Error("sixth parameter should be boolean.");
		else
			fire = args[5];
	}
		
	var world = Bukkit.getWorld(args[0]);
	if(world == null)
		throw new Error("Unknown world named "+args[0]);

	world.createExplosion(new Location(world, args[1], args[2], args[3]), new Float(power), fire);
}