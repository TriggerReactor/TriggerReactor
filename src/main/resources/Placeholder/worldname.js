function worldname(args){
	if(player == null)
		return null;
		
	return player.getWorld().getName();
}