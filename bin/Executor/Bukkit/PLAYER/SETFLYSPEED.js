function SETFLYSPEED(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETFLYSPEED")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETFLYSPEED: " + arg)
  }
  
  if (arg < 0)
  {
    throw new Error("Argument for Executor SETFLYSPEED should not be negative")
  }
  
  player.setFlySpeed(arg)
}
