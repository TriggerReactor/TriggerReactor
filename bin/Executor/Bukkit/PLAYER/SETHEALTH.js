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
  
  player.setHealth(arg)
}
