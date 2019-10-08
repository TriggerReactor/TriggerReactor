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
  
  if (arg < 1)
  {
	  throw new Error("Argument for SETMAXHEALTH is too low: " + arg + " minimum is 1")
  }
  
  var ValueContainer = Java.type("org.spongepowered.api.data.value.ValueContainer")
  
  if (!(player instanceof ValueContainer)) {
	  throw new Error("Value in player does not support max health (did you set it to something else?)")
  }
  
  var bounded = player.get(Keys.MAX_HEALTH).orElse(-1);
  
  if (bounded === -1) {
	  throw new Error("value in variable player does not support max health (did you set it to something else?)")
  }
  
  arg *= 1.0 //cast to double
  
  player.offer(Keys.MAX_HEALTH, arg)
}
