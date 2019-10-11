function SETXP(args)
{
  if (args.length !== 1)
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

  if (arg < 0 || arg > 1) {
    throw new Error("Argument for SETXP is too low: " + arg + ". minimum is 0, and maximum is 1.")
  }
  var Integer = Java.type("java.lang.Integer")
  var currentLevel = player.get(Keys.EXPERIENCE_LEVEL).orElse(0)
  if(currentLevel >= 0 && currentLevel <= 15){
    var requiredExp = 2 * currentLevel + 7
  }else if(currentLevel >= 16 && currentLevel <= 30){
    var requiredExp = 5 * currentLevel - 38
  }else if(currentLevel >= 31){
    var requiredExp = 9 * currentLevel -158
  }

  player.offer(Keys.EXPERIENCE_SINCE_LEVEL, Integer.valueOf(Math.round(requiredExp * (arg-0.02))))
}