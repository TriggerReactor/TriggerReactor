function playerloc(args){
	if(player == null)
		return null;

	loc = player.getLocation();
	world = loc.getWorld().getName();
	x = loc.getBlockX();
	y = loc.getBlockY();
	z = loc.getBlockZ();
	
	return world+"@"+x+","+y+","+z;
}