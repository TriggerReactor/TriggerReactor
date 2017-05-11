function BROADCAST(args){
    var str = "";
    for(var i = 0; i < args.length ; i++)
        str += args[i];
    Bukkit.broadcastMessage(str);
    return null;
}