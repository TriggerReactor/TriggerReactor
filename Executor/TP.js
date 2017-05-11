function TP(args){
    if(args.length == 3){
        var world;
        var x, y, z;
        world = player.getWorld();
        x = parseFloat(args[0]);
        y = parseFloat(args[1]);
        z = parseFloat(args[2]);
        
        player.teleport(new Location(world, x, y, z));
        
        return null;
    }else if(args.length == 1){
        var loc = args[0];
        player.teleport(loc);
        
        return null;
    }else{
        print("Teleport Cancelled. Invalid arguments");
        
        return Executor.STOP;
    }
}