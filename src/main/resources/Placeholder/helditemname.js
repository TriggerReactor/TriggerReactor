function helditemname(args){
	if(player == null)
		return null;
		
	if(player.getItemInHand() == null)
		return null;
		
	return player.getItemInHand().getType().name();
}