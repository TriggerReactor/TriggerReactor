function biome(args){
	if(player == null)
		return null;
	
	loc = player.getLocation();
	world = player.getWorld();
	return world.getBiome(loc.getBlockX(), loc.getBlockZ()).name();
}