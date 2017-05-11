function MESSAGE(args){
    var str = "";
    for(var i = 0; i < args.length ; i++)
        str += args[i];
    player.sendMessage(str);
    return null;
}