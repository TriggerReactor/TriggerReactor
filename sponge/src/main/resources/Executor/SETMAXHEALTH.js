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
  
  var bounded = player.get(Keys.MAX_HEALTH).orElse(-1);
  
  if (bounded === -1) {
	  throw new Error("value in variable player does not support max health (did you set it to something else?)")
  }
  
  var max = bounded.getMaxValue()
  var min = bounded.getMinValue()
  
  if (arg > max) {
	  throw new Error("Argument for SETHEALTH is too high: " + arg + " maximum is " + max)
  }
  
  if (arg < min) {
	  throw new Error("Argument for SETHEALTH is too low: " + arg + " minimum is " + min)
  }
  
  player.offer(Keys.MAX_HEALTH, arg)
}
