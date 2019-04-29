function SETXP(args)
{
  if (args.length > 1)
  {
    throw new Error("Executor SETXP should only have 1 argument")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETXP: " + arg)
  }
  
  if (arg < 0 || arg > 1)
  {
    throw new Error(arg + " is outside of the allowable range of 0..1 for executor SETXP")
  }
  
  player.setExp(arg)
}
