//3 expected args: entry: String, Objective: String, value: int

function SET(args)
{
  if (args.length !== 3)
  {
    throw new Error("Incorrect number or arguments for Executor SET")
  }
  
  var entry = args[0]
  if (typeof entry !== "string")
  {
    entry = entry.toString()
  }
  
  var objective = args[1]
  if (typeof objective !== "string")
  {
    objective = objective.toString()
  }
  
  var value = Math.round(args[2])
  
  
}
