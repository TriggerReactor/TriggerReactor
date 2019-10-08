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
  
  //a value of 0 means 1 xp for some reason
  //and a value of -1 doesn't work
  
  arg -= 1
  
  print(arg)
  
  var Integer = Java.type("java.lang.Integer")
  
  if (arg < 0) {
	  player.offer(Keys.EXPERIENCE_SINCE_LEVEL, Integer.valueOf(0))
  } else {
       player.offer(Keys.TOTAL_EXPERIENCE, Integer.valueOf(Math.round(arg)))
  }
}