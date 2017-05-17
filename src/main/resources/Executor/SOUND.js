function SOUND(args){
	if(args.length == 4){
		var location = args[0];
		var sound = args[1];
		var volume = args[2];
		var pitch = args[3];
		
		player.playSound(location, sound, volume, pitch);
	} else {
		throw new Error("Invalid parameters. Need [Location, Sound, Number, Number]")
	}
	return null;
}