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
  
  //use rounded instead of the original value.  Allows values like 3.0 to pass as whole numbers
  if (rounded < 0)
  {
    throw new Error("Argument for Executor SETFOOD should not be negative")
  }
  
  player.setFoodLevel(rounded)
}
