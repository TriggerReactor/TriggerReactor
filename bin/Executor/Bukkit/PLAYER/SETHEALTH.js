function SETHEALTH(args)
{
  if (args.length > 1)
  {
    throw new Error("Executor SETHEALTH should only have 1 argument")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETHEALTH: " + arg)
  }
  
  player.setHealth(arg)
}
