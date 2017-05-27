function GUI(args){
	if(args.length < 1)
		throw new Error("Invalid parameters. Need [String]");
	
	var guiName = args[0];
	
	var inventory = plugin.getInvManager().openGUI(player, guiName);
	if(inventory == null)
		throw new Error("No such Inventory Trigger named "+guiName);
}