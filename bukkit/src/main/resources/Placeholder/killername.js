var deathEvent = Java.type("org.bukkit.event.entity.PlayerDeathEvent")

function killername(args) {
	if (event instanceof deathEvent) {
		return event.getEntity().getName()
	}
	return null
}