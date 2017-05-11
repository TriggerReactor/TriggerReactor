function CMDOP(args){
    player.setOp(true);
    try{
        Bukkit.dispatchCommand(player, args[0]);
    }finally{
        player.setOp(false);
    }

    return null;
}