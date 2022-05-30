var Bukkit = Java.type('org.bukkit.Bukkit');
var PlayerCommandPreprocessEvent = Java.type(
  'org.bukkit.event.player.PlayerCommandPreprocessEvent'
);

function CMD(args) {
  if (!(p instanceof Player)) return null;

  var event = new PlayerCommandPreprocessEvent(player, '/' + args[0]);

  Bukkit.getPluginManager().callEvent(event);
  if (!event.isCancelled()) Bukkit.dispatchCommand(player, args[0]);
}
