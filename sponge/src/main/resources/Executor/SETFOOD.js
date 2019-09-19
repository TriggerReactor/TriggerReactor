function SETFOOD(args)
{
  if (args.length !== 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETFOOD")
  }
  
  var arg = args[0]
  
  if (!(typeof arg === "number"))
  {
    throw new Error("Invalid argument for Executor SETFOOD: " + arg)
  }
  
  var rounded = Math.round(args[0])

  if (rounded !== arg)
  {
    throw new Error("Argument for Executor SETFOOD should be a whole number")
  }
  
  var ValueContainer = Java.type("org.spongepowered.api.data.value.ValueContainer")
  
  if (!(player instanceof ValueContainer)) {
	  throw new Error("Value in player does not support food (did you set it to something else?)")
  }
  
  var bounded = player.getValue(Keys.FOOD_LEVEL).orElse(null);
  
  if (bounded === null) {
	  throw new Error("Value in player does not support food (did you set it to something else?)")
  }
  
  if (arg < bounded.getMinValue()) {
	  throw new Error("argument for executor SETFOOD is too low: " + arg + ", minimum is: " + bounded.getMinValue())
  }
  
  if (arg > bounded.getMaxValue()) {
	  throw new Error("argument for executor SETFOOD is too high: " + arg + ", maximum is: " + bounded.getMaxValue())
  }
  
  player.offer(Keys.FOOD_LEVEL, arg);
}
