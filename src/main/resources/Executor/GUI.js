function GUI(args){
	if(args.length < 1)
		throw new Error("Invalid parameters. Need [String]");
	
	var guiName = args[0];
	
	var BukkitPlayer = Java.type('io.github.wysohn.triggerreactor.bukkit.bridge.player.BukkitPlayer');
	
	var inventory = plugin.getInvManager().openGUI(new BukkitPlayer(player), guiName);
	if(inventory == null)
		throw new Error("No such Inventory Trigger named "+guiName);
}