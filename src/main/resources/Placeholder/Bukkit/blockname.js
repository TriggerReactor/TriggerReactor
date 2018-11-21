function blockname(args){
	
	if ((args.length == 0) || (args.length == 1) || (args.length == 2) || (args.length == 3)) {
		throw new Error("$blockname 플레이스 홀더는 $blockname:\"월드이름\":x:y:z 로 사용해야 합니다.");
	}else if (args.length == 4) {
		
		if (Bukkit.getWorld(args[0]) == null) {
			throw new Error("해당 월드가 존재하지 않습니다.");
			return null;
		}
		if ((typeof args[1] !== "number")||(typeof args[2] !== "number")||(typeof args[3] !== "number")) {
			throw new Error("좌표값은 정수여야 합니다.");
			return null;
		}
		world = Bukkit.getWorld(args[0]);
		x = args[1];
		y = args[2];
		z = args[3];
		
		block = world.getBlockAt(x,y,z).getType().name().toLowerCase();
		return block;
	}
	
}