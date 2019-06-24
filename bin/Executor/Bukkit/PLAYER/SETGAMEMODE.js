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
  
  try
  {
	  var GameMode = Java.type('org.bukkit.GameMode')
	  var mode = GameMode.valueOf(arg.toUpperCase())
	  player.setGameMode(mode)
  }
    catch(ex)
  {
	  throw new Error("Unknown GAEMMODE value "+arg)
  }
}
