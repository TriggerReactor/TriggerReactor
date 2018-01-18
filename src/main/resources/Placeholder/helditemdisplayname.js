function helditemdisplayname(args){
	if(player == null)
		return null;
		
	if(player.getItemInHand() == null)
		return null;
		
	var itemMeta = player.getItemInHand().getItemMeta();
	if(itemMeta == null)
		return null;
		
	if(itemMeta.getDisplayName() == null)
		return "No display name";
		
	return itemMeta.getDisplayName();
}