function CLEARENTITY(args) {
	if (player === null) return null;
	if (args.length !== 1 || typeof args[0] !== 'number') throw new Error('Invalid parameters! [Number]');

	var nearby = player.getNearbyEntities(args[0], args[0], args[0]);
	for (var i = 0; i < near.size(); i++) {
		var entity = nearby.get(i);
		entity.remove();
	};
}
