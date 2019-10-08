function CLEARPOTION(args){
	if(player === null)
		return null;

	if(args.length == 0){
		var list = player.get(Keys.POTION_EFFECTS).orElse(null);
		if(list == null)
			return null;
		list.clear();
		player.offer(Keys.POTION_EFFECTS, list);
	}else{
		var typeName = args[0].toUpperCase();
		var type = ReflectionUtil.getField(PotionEffectTypes.class, null, typeName);
			
		var list = player.get(Keys.POTION_EFFECTS).get();
		for(var iter = list.iterator(); iter.hasNext(); ){
			var effect = iter.next();
			
			if(effect.getType() == type)
				iter.remove();
		}
		player.offer(Keys.POTION_EFFECTS, list);
	}
}