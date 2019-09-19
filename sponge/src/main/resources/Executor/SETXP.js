function SETXP(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect Number of arguments for Executor SETXP")
  }
  
  var arg = args[0]
  
  if (!(typeof arg == "number"))
  {
    throw new Error("Invalid argument for SETXP: " + arg)
  }
  
  var ValueContainer = Java.type("org.spongepowered.api.data.value.ValueContainer")
  
  if (!(player instanceof ValueContainer)) {
	  throw new Error("Value in player does not support food (did you set it to something else?)")
  }
  
  if (arg < 0) {
	  throw new Error("Argument for SETXP is too low: " + arg + " minimum is 0")
  }
  
  //TODO: use total experience level
  player.offer(Keys.EXPERIENCE_SINCE_LEVEL, arg)
}