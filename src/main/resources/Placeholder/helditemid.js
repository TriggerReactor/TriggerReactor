function helditemid(args){
	if(player == null)
		return null;
		
	if(player.getItemInHand() == null)
		return null;
		
	return player.getItemInHand().getType().getId();
}