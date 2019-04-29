function SETWALKSPEED(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETWALKSPEED")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETWALKSPEED: " + arg)
  }
  
  if (arg < 0)
  {
    throw new Error("Argument for Executor SETWALKSPEED should not be negative")
  }
  
  player.setWalkSpeed(arg)
}
