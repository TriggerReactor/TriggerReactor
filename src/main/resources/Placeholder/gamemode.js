function gamemode(args){
	if(player == null)
		return null;
		
	return player.getGameMode().name();
}