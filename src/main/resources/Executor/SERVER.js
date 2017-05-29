function SERVER(args){
	if(args.length < 1)
		throw Error("Invalid Parameter. [String] required.");
	
	var serverName = args[0];
	plugin.getBungeeHelper().sendToServer(player, serverName);
}