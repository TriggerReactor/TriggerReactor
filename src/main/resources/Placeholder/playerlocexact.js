function playerlocexact(args){
	if(player == null)
		return null;

	loc = player.getLocation();
	world = loc.getWorld().getName();
	x = loc.getX();
	y = loc.getY();
	z = loc.getZ();
	
	return world+"@"+x+","+y+","+z;
}