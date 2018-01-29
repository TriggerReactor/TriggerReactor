function CLEARENTITY(args){
	if(player === null)
		return null;
		
	if(args.length != 1 || typeof args[0] !== "number")
		throw new Error("Invalid parameters! [Number]");

	var near = player.getNearbyEntities(args[0], args[0], args[0]);
	for(var i = 0; i < near.size(); i++){
		var entity = near.get(i);
		entity.remove();
	}
}