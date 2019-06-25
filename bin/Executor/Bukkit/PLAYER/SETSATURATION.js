function SETSATURATION(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETSATURATION")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETSATURATION: " + arg)
  }
  
  if (arg < 0)
  {
    throw new Error("Argument for Executor SETSATURATION should not be negative")
  }
  
  player.setSaturation(arg)
}
