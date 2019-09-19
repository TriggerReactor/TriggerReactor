function SETGAMEMODE(args)
{
  if (args.length != 1)
  {
    throw new Error("Incorrect number of arguments for executor SETGAMEMODE")
  }
  
  var arg = args[0]
  
  if (typeof arg != "string")
  {
    throw new Error("Invalid argument for Executor SETGAMEMODE: " + arg)
  }
  
  if (!(player.supports(Keys.GAME_MODE))) {
	  throw new Error("value in variable player does not support gamemodes (did you set it to something else?)")
  }
  
  var GameModes = Java.type('org.spongepowered.api.entity.living.player.gamemode.GameModes')
  try {
	  var mode = ReflectionUtil.getField(GameModes.class, null, value.toUpperCase())
  } catch(ex) {
	  throw new Error("Unknown GAEMMODE value "+value);
  }
  
  player.offer(Keys.GAME_MODE, mode);
}
