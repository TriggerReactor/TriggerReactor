function SETFLYMODE(args)
{ 
  if (args.length > 1)
  {
    throw new Error("Executor SETFLYMODE should only have 1 argument.")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "boolean"))
  {
    throw new Error("Invalid argument for executor SETFLYMODE: " + arg)
  }
  
  player.setAllowFlight(arg);
  player.setFlying(arg);
}
