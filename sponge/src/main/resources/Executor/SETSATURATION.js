function SETSATURATION(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SATURATION")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETSATURATION: " + arg)
  }
  
  var bounded = player.get(Keys.SATURATION).orElse(-1);
  
  if (bounded === -1) {
	  throw new Error("value in variable player does not support saturation (did you set it to something else?)")
  }
  
  var max = bounded.getMaxValue()
  var min = bounded.getMinValue()
  
  if (arg > max) {
	  throw new Error("Argument for SETSATURATION is too high: " + arg + " maximum is " + max)
  }
  
  if (arg < min) {
	  throw new Error("Argument for SETSATURATION is too low: " + arg + " minimum is " + min)
  }
  
  player.offer(Keys.SATURATION, arg)
}