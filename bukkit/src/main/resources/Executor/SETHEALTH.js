function SETHEALTH(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for executor SETHEALTH")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETHEALTH: " + arg)
  }
  
  if (arg < 0)
  {
    throw new Error("Argument for Exector SETHEALTH should not be negative")
  }
  
  if (arg > player.getMaxHealth())
  {
    throw new Error("Argument for Executor SETHEALTH is greater than the max health")
  }
  
  player.setHealth(arg)
}
