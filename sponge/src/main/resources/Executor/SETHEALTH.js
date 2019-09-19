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
  
  if (arg < 0)
  {
    throw new Error("Argument for Exector SETHEALTH should not be negative")
  }
  
  var ValueContainer = Java.type("org.spongepowered.api.data.value.ValueContainer")
  
  if (!(player instanceof ValueContainer)) {
	  throw new Error("Value in player does not support health (did you set it to something else?)")
  }
  
  var maxHealth = player.get(Keys.MAX_HEALTH).orElse(-1);
  
  if (maxHealth === -1) 
  {
	  throw new Error("value in variable player does not support health (did you set it to something else?)")
  }
  
  if (arg > maxHealth)
  {
    throw new Error("Argument for Executor SETHEALTH is greater than the maximum health of: " + maxHealth);
  }
  
  arg *= 1.0 //cast arg to double
  
  player.offer(Keys.HEALTH, arg);
}
