function SETMAXHEALTH(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETMAXHEALTH")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETMAXHEALTH: " + arg)
  }
  
  if (arg <= 0)
  {
    throw new Error("Argument for Executor SETMAXHEALTH should not be negative or zero")
  }
  
  if (arg > 2048)
  {
    throw new Error("Maximum health cannot be greater than 2048")
  }
  
  player.setMaxHealth(arg)
}
