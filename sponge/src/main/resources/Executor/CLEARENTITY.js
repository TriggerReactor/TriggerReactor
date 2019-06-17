function CLEARENTITY(args){
	if(player === null)
		return null;
		
	if(args.length != 1 || typeof args[0] !== "number")
		throw new Error("Invalid parameters! [Number]");

	var entities = player.getLocation().getExtent().getEntities();
	
	for(var i = 0; i < entities.size(); i++){
		var entity = entities[i];
		
		if(entity == player)
			continue;
		
		var dist = entity.getLocation().getPosition().distance(player.getLocation().getPosition());
		if(dist < args[0]){
			entity.remove();
		}
	}
}