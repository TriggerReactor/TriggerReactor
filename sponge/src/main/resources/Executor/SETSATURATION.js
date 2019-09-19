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
  
  var ValueContainer = Java.type("org.spongepowered.api.data.value.ValueContainer")
  
  if (!(player instanceof ValueContainer)) {
	  throw new Error("Value in player does not support saturation (did you set it to something else?)")
  }
  
  var bounded = player.get(Keys.SATURATION).orElse(-1);
  
  if (bounded === -1) {
	  throw new Error("value in variable player does not support saturation (did you set it to something else?)")
  }
  
  if (arg < 0) {
	  throw new Error("Argument for SETSATURATION is too low: " + arg + " minimum is 0")
  }
  
  arg *= 1.0 //cast arg to double
  
  player.offer(Keys.SATURATION, arg)
}