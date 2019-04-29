function SETFOOD(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETFOOD")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETFOOD: " + arg)
  }
  
  player.setFoodLevel(arg)
}
