var Bukkit = Java.type('org.bukkit.Bukkit');
var ChatColor = Java.type('org.bukkit.ChatColor');
var Objects = Java.type('java.util.Objects');

function BROADCAST(args) {
  var PlaceholderAPI;
  var msg = '';

  if (player === null) return null;

  for each (var str in args)
    msg += Objects.toString(str);
  msg = ChatColor.translateAlternateColorCodes('&', msg);

  if (Bukkit.getPluginManager().isPluginEnabled('PlaceholderAPI'))
    PlaceholderAPI = Java.type('me.clip.placeholderapi.PlaceholderAPI');

  player.sendMessage(PlaceholderAPI ? PlaceholderAPI.setPlaceholders(p, msg) : msg);
}
