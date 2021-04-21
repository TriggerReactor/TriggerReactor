validation =  {
	"overloads" : [
		[],
		[{"type": "string", "name": "effect type"}]
	]
}

function CLEARPOTION(args){
	if(player === null)
		return null;

	if(overloads === 0){
		var activeEffects = player.getActivePotionEffects();
		for(var iter = activeEffects.iterator(); iter.hasNext();){
			var type = iter.next().getType();
			player.removePotionEffect(type);
		}
	}else{
		var typeName = args[0].toUpperCase();
		var PotionEffectType = Java.type('org.bukkit.potion.PotionEffectType');
		var type = PotionEffectType.getByName(typeName);
		
		if(type == null)
			throw new Error("Invalid PotionEffectType named "+typeName);
			
		player.removePotionEffect(type);
	}
}